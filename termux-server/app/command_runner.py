import asyncio
import os
from typing import Callable, Optional
from app import config
from app.models import ExecutionRecord, ApprovalStatus
from app.audit_log import log_audit_event, sanitize_text

# Optional callback for streaming stdout/stderr events to WebSocket
OutputCallback = Callable[[str, str, str], None]  # (command_id, stream_name, chunk_text)

async def execute_approved_command(
    record: ExecutionRecord,
    output_callback: Optional[OutputCallback] = None
) -> ExecutionRecord:
    """Execute an approved command using fixed array arguments, strict timeout, and output bounds."""
    if record.status != ApprovalStatus.APPROVED:
        raise PermissionError(f"Cannot execute command in status '{record.status}'.")

    log_audit_event("command_execution_started", {
        "command_id": record.command_id,
        "program": record.program,
        "arguments": record.arguments
    })

    stdout_chunks: list[str] = []
    stderr_chunks: list[str] = []
    total_bytes = 0

    try:
        # Strictly use create_subprocess_exec with fixed executable and argument array (NO shell=True)
        process = await asyncio.create_subprocess_exec(
            record.program,
            *record.arguments,
            cwd=record.working_directory,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE
        )

        async def read_stream(stream, is_stderr: bool):
            nonlocal total_bytes
            stream_name = "stderr" if is_stderr else "stdout"
            chunks = stderr_chunks if is_stderr else stdout_chunks
            
            while True:
                line = await stream.readline()
                if not line:
                    break
                text = line.decode("utf-8", errors="replace")
                sanitized_text = sanitize_text(text)
                total_bytes += len(line)
                
                if total_bytes <= config.MAX_STDOUT_BYTES:
                    chunks.append(sanitized_text)
                    if output_callback:
                        output_callback(record.command_id, stream_name, sanitized_text)
                elif len(chunks) > 0 and "[TRUNCATED]" not in chunks[-1]:
                    trunc_notice = "\n[OUTPUT TRUNCATED: Exceeded 512KB Limit]\n"
                    chunks.append(trunc_notice)
                    if output_callback:
                        output_callback(record.command_id, stream_name, trunc_notice)

        # Wait with timeout
        await asyncio.wait_for(
            asyncio.gather(
                read_stream(process.stdout, False),
                read_stream(process.stderr, True),
                process.wait()
            ),
            timeout=config.COMMAND_TIMEOUT_SECONDS
        )

        record.exit_code = process.returncode
        record.stdout = "".join(stdout_chunks)
        record.stderr = "".join(stderr_chunks)

    except asyncio.TimeoutError:
        try:
            process.kill()
            await process.wait()
        except Exception:
            pass
        record.exit_code = -1
        record.stderr = (record.stderr or "") + f"\n[ERROR] Command timed out after {config.COMMAND_TIMEOUT_SECONDS} seconds."
        log_audit_event("command_execution_timeout", {"command_id": record.command_id})

    except Exception as e:
        record.exit_code = -1
        record.stderr = (record.stderr or "") + f"\n[EXECUTION ERROR] {str(e)}"
        log_audit_event("command_execution_error", {"command_id": record.command_id, "error": str(e)})

    log_audit_event("command_execution_completed", {
        "command_id": record.command_id,
        "exit_code": record.exit_code,
        "stdout_len": len(record.stdout),
        "stderr_len": len(record.stderr)
    })

    return record
