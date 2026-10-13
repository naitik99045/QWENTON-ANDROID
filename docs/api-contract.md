# API Contract & Schema Reference

Base URL: `http://127.0.0.1:8765`  
WebSocket Stream: `ws://127.0.0.1:8765/ws/events`

---

## REST Endpoints

### 1. `GET /health`
Probe server operational status.

**Response:**
```json
{
  "status": "ok",
  "service": "qwenton-apex",
  "version": "0.1.0",
  "server_time": "2026-10-13T08:35:00Z"
}
```

### 2. `POST /chat`
Dispatch user natural language prompt.

**Request:**
```json
{
  "request_id": "req_abc123",
  "conversation_id": "conv_xyz789",
  "message": "Inspect battery health",
  "client_version": "0.1.0"
}
```

**Tool Proposal Response:**
```json
{
  "type": "tool_request",
  "request_id": "req_abc123",
  "tool": "get_battery",
  "arguments": {},
  "risk": "safe",
  "requires_approval": false,
  "explanation": "Query device battery level via Termux:API."
}
```

**Command Proposal Response:**
```json
{
  "type": "command_request",
  "request_id": "req_abc123",
  "command_id": "cmd_1728800000000",
  "program": "pip",
  "arguments": ["install", "httpx"],
  "working_directory": "/data/data/com.termux/files/home",
  "risk": "medium",
  "requires_approval": true,
  "explanation": "Install Python package 'httpx' inside virtualenv."
}
```

### 3. `POST /commands/{command_id}/approve`
Submit user approval decision for a pending command proposal.

**Request:**
```json
{
  "approval_id": "appr_987",
  "approved": true,
  "approved_by": "user"
}
```

**Response:**
```json
{
  "command_id": "cmd_1728800000000",
  "approved": true,
  "status": "approved",
  "message": "Command executed with exit code 0."
}
```

### 4. `POST /commands/{command_id}/cancel`
Cancel or reject a pending command proposal.

---

## WebSocket Protocol (`/ws/events`)

Streamed events:

- Handshake:
  `{"type": "connected"}`
- Assistant Stream Chunk:
  `{"type": "assistant_delta", "request_id": "req_1", "text": "Processing request..."}`
- Command Execution Output:
  `{"type": "command_output", "command_id": "cmd_1", "stream": "stdout", "text": "Installed 1 package."}`
- Command Completion:
  `{"type": "command_completed", "command_id": "cmd_1", "exit_code": 0}`
