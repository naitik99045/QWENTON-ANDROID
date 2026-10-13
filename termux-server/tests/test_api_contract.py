import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_chat_empty_message_validation():
    response = client.post("/chat", json={
        "request_id": "req_1",
        "conversation_id": "conv_1",
        "message": "   "
    })
    assert response.status_code == 400

def test_chat_tool_proposal_battery():
    response = client.post("/chat", json={
        "request_id": "req_battery",
        "conversation_id": "conv_1",
        "message": "Can you check my battery?"
    })
    assert response.status_code == 200
    data = response.json()
    assert data["type"] == "tool_request"
    assert data["tool"] == "get_battery"
    assert data["risk"] == "safe"
    assert data["requires_approval"] is False

def test_chat_command_proposal_python_install():
    response = client.post("/chat", json={
        "request_id": "req_pkg",
        "conversation_id": "conv_1",
        "message": "Install python httpx package"
    })
    assert response.status_code == 200
    data = response.json()
    assert data["type"] == "command_request"
    assert data["program"] == "pip"
    assert data["requires_approval"] is True
    assert "command_id" in data

def test_command_approval_unknown_id():
    response = client.post("/commands/cmd_nonexistent/approve", json={
        "approval_id": "appr_1",
        "approved": True
    })
    assert response.status_code == 404

def test_history_endpoint_returns_json():
    response = client.get("/history")
    assert response.status_code == 200
    assert "events" in response.json()

def test_chat_oversized_payload_rejection():
    oversized = "A" * 35000
    response = client.post("/chat", json={
        "request_id": "req_big",
        "conversation_id": "conv_1",
        "message": oversized
    })
    assert response.status_code == 413

def test_command_approval_replay_rejected():
    # 1. Propose command
    chat_resp = client.post("/chat", json={
        "request_id": "req_cmd_test",
        "conversation_id": "conv_1",
        "message": "Install python fastapi"
    })
    cmd_id = chat_resp.json()["command_id"]

    # 2. Reject first
    appr1 = client.post(f"/commands/{cmd_id}/approve", json={
        "approval_id": "appr_1",
        "approved": False
    })
    assert appr1.status_code == 200

    # 3. Attempt replay / re-approval -> must return 409 Conflict
    appr2 = client.post(f"/commands/{cmd_id}/approve", json={
        "approval_id": "appr_2",
        "approved": True
    })
    assert appr2.status_code == 409
