from enum import Enum
from typing import Any, Optional
from datetime import datetime
from pydantic import BaseModel, Field

class RiskLevel(str, Enum):
    SAFE = "safe"
    MEDIUM = "medium"
    HIGH = "high"
    BLOCKED = "blocked"

class ApprovalStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"
    EXPIRED = "expired"

# Health Models
class HealthResponse(BaseModel):
    status: str = "ok"
    service: str = "qwenton-apex"
    version: str = "0.1.0"
    server_time: str

# Chat Request & Responses
class ChatRequest(BaseModel):
    request_id: str
    conversation_id: str
    message: str
    client_version: Optional[str] = "0.2.0"
    context_path: Optional[str] = "."

class AssistantMessageResponse(BaseModel):
    type: str = "assistant_message"
    request_id: str
    text: str
    requires_approval: bool = False

class ToolRequestResponse(BaseModel):
    type: str = "tool_request"
    request_id: str
    tool: str
    arguments: dict[str, Any] = Field(default_factory=dict)
    risk: RiskLevel = RiskLevel.SAFE
    requires_approval: bool = False
    explanation: str

class CommandRequestResponse(BaseModel):
    type: str = "command_request"
    request_id: str
    command_id: str
    program: str
    arguments: list[str] = Field(default_factory=list)
    working_directory: str
    risk: RiskLevel = RiskLevel.MEDIUM
    requires_approval: bool = True
    explanation: str

# Command Approval Models
class CommandApprovalRequest(BaseModel):
    approval_id: str
    approved: bool
    approved_by: str = "user"

class CommandApprovalResponse(BaseModel):
    command_id: str
    approved: bool
    status: ApprovalStatus
    message: str

# Execution Record
class ExecutionRecord(BaseModel):
    command_id: str
    request_id: str
    tool: str
    program: str
    arguments: list[str]
    working_directory: str
    risk: RiskLevel
    requires_approval: bool
    explanation: str
    integrity_hash: str
    created_at: datetime
    expires_at: datetime
    status: ApprovalStatus = ApprovalStatus.PENDING
    exit_code: Optional[int] = None
    stdout: str = ""
    stderr: str = ""

# WebSocket Event Model
class WebSocketEvent(BaseModel):
    type: str
    request_id: Optional[str] = None
    command_id: Optional[str] = None
    stream: Optional[str] = None
    text: Optional[str] = None
    exit_code: Optional[int] = None
    code: Optional[str] = None
    message: Optional[str] = None
