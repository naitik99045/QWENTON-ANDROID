import json
import httpx
from typing import Any, Union
from app import config
from app.models import AssistantMessageResponse, ToolRequestResponse, CommandRequestResponse, RiskLevel
from app.command_policy import create_command_proposal

class AIClient:
    """Provider-agnostic AI client interface (Mock, Remote API, or Local LLM)."""

    def __init__(self, provider: str | None = None):
        self._custom_provider = provider

    @property
    def provider(self) -> str:
        return (self._custom_provider or config.AI_PROVIDER).lower()

    async def generate_response(
        self,
        request_id: str,
        conversation_id: str,
        message: str,
        context_path: str = "."
    ) -> Union[AssistantMessageResponse, ToolRequestResponse, CommandRequestResponse]:
        """Generate response or tool proposal based on user input and workspace context."""
        msg_lower = message.strip().lower()

        # 0. Deterministic Intent: Open Installed Android Application
        if msg_lower.startswith("open ") or msg_lower.startswith("launch "):
            app_target = msg_lower.replace("open ", "").replace("launch ", "").strip()
            return ToolRequestResponse(
                request_id=request_id,
                tool="open_app",
                arguments={"app_name": app_target},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation=f"Launch application '{app_target}' on Android device."
            )

        # 0b. Deterministic Intent: Phone Calling & Contact Dialing
        if msg_lower.startswith("call ") or msg_lower.startswith("dial "):
            contact_target = msg_lower.replace("call ", "").replace("dial ", "").strip()
            return ToolRequestResponse(
                request_id=request_id,
                tool="call_contact",
                arguments={"contact_name": contact_target},
                risk=RiskLevel.MEDIUM,
                requires_approval=True,
                explanation=f"Search contacts and initiate phone call to '{contact_target}'."
            )

        # 0c. Deterministic Intent: Wireless Debugging Auto-Pairing
        if "pairing code" in msg_lower or "pair wireless" in msg_lower or "pair adb" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="pair_wireless_debugging",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Inspect Notification Listener for intercepted Wireless Debugging pairing code."
            )

        # 1. Deterministic Intent: Security Pentest & SAST Scan
        if "pentest" in msg_lower or "security audit" in msg_lower or "vulnerability" in msg_lower or "sast" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="run_sast_scan",
                arguments={"path": context_path},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation=f"Execute automated defensive SAST security scan across workspace: {context_path}"
            )

        # 2. Deterministic Intent: Port Security Audit
        if "port scan" in msg_lower or "open ports" in msg_lower or "port audit" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="run_port_audit",
                arguments={"host": "127.0.0.1"},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Audit local loopback network ports for active or exposed services."
            )

        # 3. Deterministic Intent: ADB Phone Control & Status
        if "adb" in msg_lower or "phone control" in msg_lower or "device status" in msg_lower:
            proposal = create_command_proposal(
                request_id=request_id,
                tool_name="adb_command",
                program="adb",
                arguments=["devices", "-l"],
                working_directory=config.TERMUX_HOME,
                custom_explanation="Inspect connected Android ADB device status and authorized interfaces."
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

        # 4. Deterministic Intent: Workspace File Tree Exploration
        if "tree" in msg_lower or "workspace" in msg_lower or "select folder" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="workspace_tree",
                arguments={"path": context_path},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation=f"Explore folder structure in active workspace: {context_path}"
            )

        # 5. Deterministic Intent: Battery Status via Termux:API
        if "battery" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="get_battery",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Query device battery level, temperature, and charging status via Termux:API."
            )

        # 6. Deterministic Intent: System Clock & UTC Time
        if "time" in msg_lower or "date" in msg_lower:
            return ToolRequestResponse(
                request_id=request_id,
                tool="get_time",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="Inspect current local and UTC system time."
            )

        # 7. Deterministic Intent: List Home Files
        if "list files" in msg_lower or "list home" in msg_lower or msg_lower == "ls":
            return ToolRequestResponse(
                request_id=request_id,
                tool="list_home",
                arguments={},
                risk=RiskLevel.SAFE,
                requires_approval=False,
                explanation="List files inside your Termux home directory."
            )

        # 8. Deterministic Intent: Install Python Package
        if "install" in msg_lower and "python" in msg_lower:
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

        # 9. Local Offline Model (llama.cpp in Termux)
        if self.provider == "local":
            try:
                model_name = getattr(config, "AI_MODEL", "llama-3.2-3b-q4")
                return await self._call_openai_compatible(
                    request_id=request_id,
                    endpoint=f"{config.LOCAL_LLM_URL.rstrip('/')}/chat/completions",
                    headers={"Content-Type": "application/json"},
                    message=message,
                    model=model_name
                )
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[Local LLM Offline]: Could not reach llama-server at {config.LOCAL_LLM_URL}. Make sure llama-server is running."
                )

        # 10. Remote Gemini Provider
        if self.provider == "gemini" and config.GEMINI_API_KEY:
            try:
                model_name = getattr(config, "AI_MODEL", "gemini-1.5-flash")
                return await self._call_gemini(request_id, message, model_name)
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[Gemini Error]: {str(e)}. Falling back to local assistant."
                )

        # 11. Remote OpenAI Provider
        if self.provider == "openai" and config.OPENAI_API_KEY:
            try:
                model_name = getattr(config, "AI_MODEL", "gpt-4o-mini")
                return await self._call_openai_compatible(
                    request_id=request_id,
                    endpoint="https://api.openai.com/v1/chat/completions",
                    headers={
                        "Authorization": f"Bearer {config.OPENAI_API_KEY}",
                        "Content-Type": "application/json"
                    },
                    message=message,
                    model=model_name
                )
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[OpenAI Error]: {str(e)}."
                )

        # 12. Custom Online Provider (Base URL + API Key) - e.g. OpenRouter, Groq, DeepSeek, Together, etc.
        if self.provider == "custom" and getattr(config, "CUSTOM_BASE_URL", ""):
            try:
                base_url = config.CUSTOM_BASE_URL.strip().rstrip("/")
                endpoint = base_url if base_url.endswith("/chat/completions") else f"{base_url}/chat/completions"
                headers = {"Content-Type": "application/json"}
                custom_key = getattr(config, "CUSTOM_API_KEY", "")
                if custom_key:
                    headers["Authorization"] = f"Bearer {custom_key}"
                
                model_name = getattr(config, "AI_MODEL", "gpt-4o-mini")
                return await self._call_openai_compatible(
                    request_id=request_id,
                    endpoint=endpoint,
                    headers=headers,
                    message=message,
                    model=model_name
                )
            except Exception as e:
                return AssistantMessageResponse(
                    request_id=request_id,
                    text=f"[Custom Provider Error]: {str(e)}."
                )

        # Default Mock Response with guidance
        return AssistantMessageResponse(
            request_id=request_id,
            text=f"Qwenton Apex received: '{message}'. Active workspace: '{context_path}'. I can code, run SAST pentest audits, inspect device telemetry, or automate ADB shell tasks with your approval.",
            requires_approval=False
        )

    async def _call_gemini(self, request_id: str, message: str, model: str = "gemini-1.5-flash") -> AssistantMessageResponse:
        url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={config.GEMINI_API_KEY}"
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
                {"role": "system", "content": "You are Qwenton Apex, an on-device AI assistant with coding, pentesting, and device automation capabilities."},
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
