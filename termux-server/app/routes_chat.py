from typing import Union
from fastapi import APIRouter, HTTPException
from app.models import (
    ChatRequest,
    AssistantMessageResponse,
    ToolRequestResponse,
    CommandRequestResponse
)
from app.ai_client import AIClient
from app.audit_log import log_audit_event

router = APIRouter()
ai_client = AIClient()

@router.post("/chat", response_model=Union[AssistantMessageResponse, ToolRequestResponse, CommandRequestResponse])
async def chat_endpoint(request: ChatRequest):
    """Receive user chat and return assistant message, tool proposal, or command proposal."""
    if not request.message.strip():
        raise HTTPException(status_code=400, detail="Message cannot be empty.")

    if len(request.message) > 32768:
        raise HTTPException(status_code=413, detail="Message exceeds maximum allowed length (32KB).")

    if len(request.request_id) > 128 or len(request.conversation_id) > 128:
        raise HTTPException(status_code=400, detail="Identifier exceeds maximum allowed length.")

    log_audit_event("chat_request_received", {
        "request_id": request.request_id,
        "conversation_id": request.conversation_id,
        "message": request.message
    })

    response = await ai_client.generate_response(
        request_id=request.request_id,
        conversation_id=request.conversation_id,
        message=request.message
    )

    log_audit_event("chat_response_dispatched", {
        "request_id": request.request_id,
        "response_type": response.type
    })

    return response
