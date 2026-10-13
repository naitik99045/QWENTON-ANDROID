import pytest
import sys
from app.models import ExecutionRecord, RiskLevel, ApprovalStatus
from app.command_runner import execute_approved_command
from datetime import datetime, timezone, timedelta

@pytest.mark.asyncio
async def test_execute_approved_command_success():
    now = datetime.now(timezone.utc)
    record = ExecutionRecord(
        command_id="cmd_test_runner",
        request_id="req_test_runner",
        tool="test_tool",
        program=sys.executable,
        arguments=["-c", "print('Apex Server OK')"],
        working_directory=".",
        risk=RiskLevel.SAFE,
        requires_approval=False,
        explanation="Testing safe runner execution",
        integrity_hash="dummy_hash",
        created_at=now,
        expires_at=now + timedelta(seconds=60),
        status=ApprovalStatus.APPROVED
    )

    completed = await execute_approved_command(record)
    assert completed.exit_code == 0
    assert "Apex Server OK" in completed.stdout

@pytest.mark.asyncio
async def test_unapproved_command_raises_error():
    now = datetime.now(timezone.utc)
    record = ExecutionRecord(
        command_id="cmd_unapproved",
        request_id="req_unapproved",
        tool="test_tool",
        program=sys.executable,
        arguments=["-c", "print('Should not run')"],
        working_directory=".",
        risk=RiskLevel.MEDIUM,
        requires_approval=True,
        explanation="Unapproved test",
        integrity_hash="dummy_hash",
        created_at=now,
        expires_at=now + timedelta(seconds=60),
        status=ApprovalStatus.PENDING
    )

    with pytest.raises(PermissionError):
        await execute_approved_command(record)
