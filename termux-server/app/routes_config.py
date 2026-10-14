import re
from pathlib import Path
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app import config
from app.audit_log import log_audit_event

router = APIRouter(prefix="/config", tags=["Configuration"])

class AiConfigRequest(BaseModel):
    provider: str
    api_key: str
    model: str = ""
    base_url: str = ""

class AiConfigResponse(BaseModel):
    status: str
    provider: str
    model: str
    base_url: str = ""
    api_key_masked: str | None = None

def _mask_secret(secret: str) -> str:
    if not secret or len(secret) < 8:
        return "********" if secret else "NOT_CONFIGURED"
    return secret[:4] + "..." + secret[-4:]

def _update_env_file(updates: dict[str, str]):
    env_file = Path(__file__).resolve().parent.parent / ".env"
    lines = []
    existing_keys = set()
    
    if env_file.exists():
        with open(env_file, "r", encoding="utf-8") as f:
            for line in f:
                stripped = line.strip()
                if "=" in stripped and not stripped.startswith("#"):
                    k, _ = stripped.split("=", 1)
                    k = k.strip()
                    if k in updates:
                        lines.append(f"{k}={updates[k]}\n")
                        existing_keys.add(k)
                        continue
                lines.append(line)

    for k, v in updates.items():
        if k not in existing_keys:
            lines.append(f"{k}={v}\n")

    with open(env_file, "w", encoding="utf-8") as f:
        f.writelines(lines)

@router.get("/ai-provider", response_model=AiConfigResponse)
async def get_ai_config():
    current_key = ""
    prov = config.AI_PROVIDER.lower()
    if prov == "gemini":
        current_key = config.GEMINI_API_KEY
    elif prov == "openai":
        current_key = config.OPENAI_API_KEY
    elif prov == "anthropic":
        current_key = config.ANTHROPIC_API_KEY
    elif prov == "custom":
        current_key = getattr(config, "CUSTOM_API_KEY", "")

    return AiConfigResponse(
        status="ok",
        provider=config.AI_PROVIDER,
        model=getattr(config, "AI_MODEL", "gemini-1.5-pro"),
        base_url=getattr(config, "CUSTOM_BASE_URL", ""),
        api_key_masked=_mask_secret(current_key)
    )

@router.post("/ai-provider", response_model=AiConfigResponse)
async def update_ai_config(payload: AiConfigRequest):
    prov = payload.provider.lower().strip()
    valid_providers = {"gemini", "openai", "anthropic", "local", "custom"}
    if prov not in valid_providers:
        raise HTTPException(status_code=400, detail=f"Invalid provider. Supported: {valid_providers}")

    default_model = "gemini-1.5-pro" if prov == "gemini" else ("llama-3.2-3b-q4" if prov == "local" else "gpt-4o")
    model_name = payload.model.strip() or default_model

    env_updates = {
        "AI_PROVIDER": prov,
        "AI_MODEL": model_name
    }

    if payload.base_url.strip():
        env_updates["CUSTOM_BASE_URL"] = payload.base_url.strip()
        config.CUSTOM_BASE_URL = payload.base_url.strip()

    if prov == "gemini":
        env_updates["GEMINI_API_KEY"] = payload.api_key.strip()
        config.GEMINI_API_KEY = payload.api_key.strip()
    elif prov == "openai":
        env_updates["OPENAI_API_KEY"] = payload.api_key.strip()
        config.OPENAI_API_KEY = payload.api_key.strip()
    elif prov == "anthropic":
        env_updates["ANTHROPIC_API_KEY"] = payload.api_key.strip()
        config.ANTHROPIC_API_KEY = payload.api_key.strip()
    elif prov == "custom":
        env_updates["CUSTOM_API_KEY"] = payload.api_key.strip()
        config.CUSTOM_API_KEY = payload.api_key.strip()
        if payload.base_url.strip():
            env_updates["CUSTOM_BASE_URL"] = payload.base_url.strip()
            config.CUSTOM_BASE_URL = payload.base_url.strip()

    config.AI_PROVIDER = prov
    config.AI_MODEL = env_updates["AI_MODEL"]

    _update_env_file(env_updates)

    log_audit_event("ai_provider_configured", {
        "provider": prov,
        "model": config.AI_MODEL,
        "has_base_url": bool(payload.base_url.strip())
    })

    return AiConfigResponse(
        status="ok",
        provider=prov,
        model=config.AI_MODEL,
        base_url=getattr(config, "CUSTOM_BASE_URL", ""),
        api_key_masked=_mask_secret(payload.api_key)
    )
