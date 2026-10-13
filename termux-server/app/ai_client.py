import json
import httpx
from typing import Any, Union
from app import config
from app.models import AssistantMessageResponse, ToolRequestResponse, CommandRequestResponse, RiskLevel
from app.command_policy import create_command_proposal

class AIClient:
    """Provider-agnostic AI client interface (Mock, Remote API, or Local LLM)."""

    def __init__(self, provider: str = config.AI_PROVIDER):
        self.provider = provider.lower()

    async def generate_response(
        self,
        request_id: str,
        conversation_id: str,
        message: str
    ) -> Union[AssistantMessageResponse, ToolRequestResponse, CommandRequestResponse]:
        """Generate response or tool proposal based on user input."""
        msg_lower = message.strip().lower()

        # Deterministic intent checks for built-in capabilities
        if "battery" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="get_battery",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Query device battery level and charging status via Termux:API."
            )

        if "time" in msg_lower or "date" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="get_time",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Inspect current local and UTC system time."
            )

        if "list files" in msg_lower or "list home" in msg_lower or "ls" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="list_home",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="List files inside your Termux home directory."
            )

        if "install" in msg_lower and "python" in msg_lower:
            # Parse package name if present
            words = msg_lower.split()
            pkg = words[-1] if len(words) > 1 and words[-1] not in ["python", "install", "package"] else "requests"
            proposal = create_command_proposal(
                request_id=request_id,
                tool_name="install_python_package",
                program="pip",
                arguments=["install", pkg],
                custom_explanation=f"Install Python package '{pkg}' inside Termux virtualenv."
            )
            return CommandRequestResponse(
                request_id=request_id,
                command_id=proposal.command_id,
                program=proposal.program,
                arguments=proposal.arguments,
                working_directory=proposal.working_directory,
                risk=proposal.risk,
                requires_approval=True,
                explanation=proposal.explanation
            )

        # 1. Local Offline Model (llama.cpp in Termux)
        if self.provider == "local":
            try:
                return await self._call_openai_compatible(
                    request_id=request_id,
                    endpoint=f"{config.LOCAL_LLM_URL.rstrip('/')}/chat/completions",
                    headers={"Content-Type": "application/json"},
                    message=message,
                    model="local-model"
                )
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[Local LLM Offline]: Could not reach llama-server at {config.LOCAL_LLM_URL}. Make sure llama-server is running."
                )

        # 2. Remote Gemini Provider
        if self.provider == "gemini" and config.GEMINI_API_KEY:
            try:
                return await self._call_gemini(request_id, message)
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[Gemini Error]: {str(e)}. Falling back to local assistant."
                )

        # 3. Remote OpenAI Provider
        if self.provider == "openai" and config.OPENAI_API_KEY:
            try:
                return await self._call_openai_compatible(
                    request_id=request_id,
                    endpoint="https://api.openai.com/v1/chat/completions",
                    headers={
                        "Authorization": f"Bearer {config.OPENAI_API_KEY}",
                        "Content-Type": "application/json"
                    },
                    message=message,
                    model="gpt-4o-mini"
                )
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[OpenAI Error]: {str(e)}."
                )

        # Default standard mock assistant response
        return AssistantMessageResponse(
            request_id=request_id,
            text=f"I received your request: '{message}'. As your Qwenton Apex assistant, I can check battery status, inspect files, or run approved automation.",
            requires_approval=False
        )

    async def _call_gemini(self, request_id: str, message: str) -> AssistantMessageResponse:
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={config.GEMINI_API_KEY}"
        payload = {
            "contents": [{
                "parts": [{"text": message}]
            }]
        }
        async with httpx.AsyncClient(timeout=15.0) as client:
            resp = await client.post(url, json=payload)
            resp.raise_for_status()
            data = resp.json()
            reply = data["candidates"][0]["content"]["parts"][0]["text"]
            return AssistantMessageResponse(
                request_id=request_id,
                text=reply,
                requires_approval=False
            )

    async def _call_openai_compatible(
        self,
        request_id: str,
        endpoint: str,
        headers: dict[str, str],
        message: str,
        model: str
    ) -> AssistantMessageResponse:
        payload = {
            "model": model,
            "messages": [
                {"role": "system", "content": "You are Qwenton Apex, an on-device Android assistant running locally."},
                {"role": "user", "content": message}
            ],
            "temperature": 0.7
        }
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(endpoint, json=payload, headers=headers)
            resp.raise_for_status()
            data = resp.json()
            reply = data["choices"][0]["message"]["content"]
            return AssistantMessageResponse(
                request_id=request_id,
                text=reply,
                requires_approval=False
            )
