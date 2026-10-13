import pytest
from app.command_policy import (
    create_command_proposal,
    get_and_validate_proposal,
    is_path_allowed
)
from app.models import RiskLevel

def test_blocked_program_rejection():
    with pytest.raises(ValueError, match="explicitly blocked"):
        create_command_proposal(
            request_id="req_test",
            tool_name="delete_tool",
            program="rm",
            arguments=["-rf", "/data"]
        )

def test_path_traversal_rejection():
    with pytest.raises(ValueError, match="Path traversal detected"):
        create_command_proposal(
            request_id="req_test",
            tool_name="read_allowed_file",
            program="cat",
            arguments=["../../etc/passwd"]
        )

def test_valid_proposal_integrity_and_retrieval():
    proposal = create_command_proposal(
        request_id="req_valid",
        tool_name="get_time",
        program="date",
        arguments=["+%Y-%m-%d"]
    )
    assert proposal.command_id.startswith("cmd_")
    assert proposal.integrity_hash is not None

    # Retrieve and validate
    record = get_and_validate_proposal(proposal.command_id)
    assert record.command_id == proposal.command_id
    assert record.risk == RiskLevel.SAFE

def test_path_allowlist_check():
    assert is_path_allowed("/data/data/com.termux/files/home/test.txt")
    assert not is_path_allowed("/data/data/com.termux/files/home/../private")

def test_argument_canonicalization_with_commas():
    # Verify arguments with commas are disambiguated cleanly
    p1 = create_command_proposal(
        request_id="req_c1",
        tool_name="get_time",
        program="date",
        arguments=["a,b", "c"]
    )
    p2 = create_command_proposal(
        request_id="req_c2",
        tool_name="get_time",
        program="date",
        arguments=["a", "b,c"]
    )
    # The two proposals must have different integrity signatures
    assert p1.integrity_hash != p2.integrity_hash
