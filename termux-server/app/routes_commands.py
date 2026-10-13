from fastapi import APIRouter, HTTPException, status
from app.models import CommandApprovalRequest, CommandApprovalResponse, ApprovalStatus
from app.command_policy import get_and_validate_proposal
from app.command_runner import execute_approved_command
from app.audit_log import log_audit_event

router = APIRouter()

@router.post("/commands/{command_id}/approve", response_model=CommandApprovalResponse)
async def approve_command(command_id: str, approval: CommandApprovalRequest):
    """Verify integrity and execute an approved command."""
    try:
        record = get_and_validate_proposal(command_id)
    except KeyError:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Command not found.")
    except TimeoutError:
        raise HTTPException(status_code=status.HTTP_410_GONE, detail="Command has expired.")
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

    if record.status != ApprovalStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Command '{command_id}' is already in status '{record.status}'. Re-execution is prohibited."
        )

    if not approval.approved:
        record.status = ApprovalStatus.REJECTED
        log_audit_event("command_rejected_by_user", {"command_id": command_id, "by": approval.approved_by})
        return CommandApprovalResponse(
            command_id=command_id,
            approved=False,
            status=ApprovalStatus.REJECTED,
            message="Command rejected by user."
        )

    # Atomically lock status to APPROVED before execution
    record.status = ApprovalStatus.APPROVED
    log_audit_event("command_approved_by_user", {"command_id": command_id, "by": approval.approved_by})

    # Execute asynchronously
    await execute_approved_command(record)

    return CommandApprovalResponse(
        command_id=command_id,
        approved=True,
        status=ApprovalStatus.APPROVED,
        message=f"Command executed with exit code {record.exit_code}."
    )

@router.post("/commands/{command_id}/cancel", response_model=CommandApprovalResponse)
async def cancel_command(command_id: str):
    """Cancel a pending command proposal."""
    try:
        record = get_and_validate_proposal(command_id)
        record.status = ApprovalStatus.REJECTED
        log_audit_event("command_canceled", {"command_id": command_id})
        return CommandApprovalResponse(
            command_id=command_id,
            approved=False,
            status=ApprovalStatus.REJECTED,
            message="Command successfully cancelled."
        )
    except KeyError:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Command not found.")
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
