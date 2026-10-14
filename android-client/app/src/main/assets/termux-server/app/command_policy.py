import hashlib
import hmac
import json
import os
from datetime import datetime, timezone, timedelta
from typing import Optional
from app import config
from app.models import RiskLevel, ExecutionRecord, ApprovalStatus
from app.audit_log import log_audit_event

# Blocked programs and operations
BLOCKED_PROGRAMS = {
    "rm", "rmdir", "del", "format", "mkfs",
    "sudo", "su", "chmod", "chown", "chgrp",
    "kill", "pkill", "killall", "reboot", "shutdown",
    "dd", "fdisk", "mkswap", "bash", "sh", "zsh"
}

# Registered tools with default risks
TOOL_REGISTRY = {
    "get_time": {
        "program": "date",
        "default_args": ["+%Y-%m-%d %H:%M:%S"],
        "risk": RiskLevel.SAFE,
        "requires_approval": False
    },
    "get_battery": {
        "program": "termux-battery-status",
        "default_args": [],
        "risk": RiskLevel.SAFE,
        "requires_approval": False
    },
    "list_home": {
        "program": "ls",
        "default_args": ["-la", config.TERMUX_HOME],
        "risk": RiskLevel.SAFE,
        "requires_approval": False
    },
    "read_allowed_file": {
        "program": "cat",
        "default_args": [],
        "risk": RiskLevel.MEDIUM,
        "requires_approval": True
    },
    "install_python_package": {
        "program": "pip",
        "default_args": ["install"],
        "risk": RiskLevel.MEDIUM,
        "requires_approval": True
    },
    "run_known_script": {
        "program": "bash",
        "default_args": [],
        "risk": RiskLevel.HIGH,
        "requires_approval": True
    },
    "workspace_tree": {
        "program": "tree",
        "default_args": [],
        "risk": RiskLevel.SAFE,
        "requires_approval": False
    },
    "patch_file": {
        "program": "patch",
        "default_args": [],
        "risk": RiskLevel.MEDIUM,
        "requires_approval": True
    },
    "write_file": {
        "program": "write",
        "default_args": [],
        "risk": RiskLevel.MEDIUM,
        "requires_approval": True
    },
    "run_sast_scan": {
        "program": "sast",
        "default_args": [],
        "risk": RiskLevel.SAFE,
        "requires_approval": False
    },
    "adb_command": {
        "program": "adb",
        "default_args": [],
        "risk": RiskLevel.HIGH,
        "requires_approval": True
    },
    "run_build": {
        "program": "python",
        "default_args": [],
        "risk": RiskLevel.MEDIUM,
        "requires_approval": True
    }
}

# In-memory execution registry: command_id -> ExecutionRecord
_PENDING_EXECUTIONS: dict[str, ExecutionRecord] = {}

# Server secret salt for command HMAC
_SECRET_KEY = os.urandom(32)

def calculate_integrity_hash(
    command_id: str,
    program: str,
    arguments: list[str],
    working_directory: str,
    expires_at_iso: str
) -> str:
    """Calculate HMAC-SHA256 hash for command parameters to guarantee tamper resistance."""
    canonical_args = json.dumps(arguments, separators=(',', ':'))
    canonical_payload = f"{command_id}:{program}:{canonical_args}:{working_directory}:{expires_at_iso}"
    return hmac.new(_SECRET_KEY, canonical_payload.encode("utf-8"), hashlib.sha256).hexdigest()

def is_path_allowed(path_str: str) -> bool:
    """Verify that path does not escape allowed directories, perform path traversal, or bypass via symlinks."""
    if ".." in path_str:
        return False
    normalized = os.path.realpath(os.path.expanduser(path_str))
    for allowed in config.ALLOWED_DIRECTORIES:
        allowed_norm = os.path.realpath(os.path.expanduser(allowed))
        if normalized == allowed_norm or normalized.startswith(allowed_norm + os.sep):
            return True
    return False

def create_command_proposal(
    request_id: str,
    tool_name: str,
    program: str,
    arguments: list[str],
    working_directory: Optional[str] = None,
    custom_explanation: Optional[str] = None
) -> ExecutionRecord:
    """Validate parameters and register a pending command proposal with SHA-256 integrity hash."""
    # 1. Blocked program check
    prog_base = os.path.basename(program).lower()
    if prog_base in BLOCKED_PROGRAMS and tool_name != "run_known_script":
        raise ValueError(f"Program '{prog_base}' is explicitly blocked by deterministic security policy.")

    # 2. Working directory validation
    cwd = working_directory or config.TERMUX_HOME
    if not is_path_allowed(cwd):
        raise ValueError(f"Working directory '{cwd}' is outside allowed directories.")

    # 3. Path traversal arguments check
    for arg in arguments:
        if ".." in arg:
            raise ValueError("Path traversal detected in command arguments ('..').")

    # 4. Tool registration check
    tool_spec = TOOL_REGISTRY.get(tool_name)
    risk = tool_spec["risk"] if tool_spec else RiskLevel.MEDIUM
    requires_approval = tool_spec["requires_approval"] if tool_spec else True

    now = datetime.now(timezone.utc)
    expires_at = now + timedelta(seconds=config.APPROVAL_TIMEOUT_SECONDS)
    command_id = f"cmd_{int(now.timestamp() * 1000)}"

    integrity_hash = calculate_integrity_hash(
        command_id=command_id,
        program=program,
        arguments=arguments,
        working_directory=cwd,
        expires_at_iso=expires_at.isoformat()
    )

    explanation = custom_explanation or f"Execute {program} for tool '{tool_name}'."

    record = ExecutionRecord(
        command_id=command_id,
        request_id=request_id,
        tool=tool_name,
        program=program,
        arguments=arguments,
        working_directory=cwd,
        risk=risk,
        requires_approval=requires_approval,
        explanation=explanation,
        integrity_hash=integrity_hash,
        created_at=now,
        expires_at=expires_at,
        status=ApprovalStatus.PENDING if requires_approval else ApprovalStatus.APPROVED
    )

    _PENDING_EXECUTIONS[command_id] = record
    log_audit_event("command_proposal_created", record.model_dump(mode="json"))
    return record

def get_and_validate_proposal(command_id: str) -> ExecutionRecord:
    """Retrieve pending proposal, verify timestamp and integrity."""
    record = _PENDING_EXECUTIONS.get(command_id)
    if not record:
        raise KeyError(f"Command '{command_id}' not found.")

    now = datetime.now(timezone.utc)
    if now > record.expires_at:
        record.status = ApprovalStatus.EXPIRED
        log_audit_event("command_proposal_expired", {"command_id": command_id})
        raise TimeoutError(f"Command '{command_id}' has expired.")

    # Re-verify integrity hash
    expected_hash = calculate_integrity_hash(
        command_id=record.command_id,
        program=record.program,
        arguments=record.arguments,
        working_directory=record.working_directory,
        expires_at_iso=record.expires_at.isoformat()
    )

    if not hmac.compare_digest(record.integrity_hash, expected_hash):
        log_audit_event("command_integrity_violation", {"command_id": command_id})
        raise ValueError("Command integrity verification failed: record has been tampered with.")

    return record
