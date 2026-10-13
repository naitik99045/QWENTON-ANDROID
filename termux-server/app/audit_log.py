import json
import re
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

# Regular expressions to scrub API keys and bearer tokens
SECRET_PATTERNS = [
    re.compile(r"AIza[0-9A-Za-z-_]{35}"),
    re.compile(r"sk-[a-zA-Z0-9]{32,}"),
    re.compile(r"Bearer\s+[a-zA-Z0-9_\-\.]{20,}", re.IGNORECASE),
]

LOG_FILE = Path(__file__).resolve().parent.parent / "audit.jsonl"

def sanitize_text(text: str) -> str:
    """Sanitize sensitive secrets from text logs."""
    sanitized = text
    for pattern in SECRET_PATTERNS:
        sanitized = pattern.sub("[REDACTED_SECRET]", sanitized)
    return sanitized

def log_audit_event(event_type: str, payload: dict[str, Any]) -> None:
    """Append a sanitized structured audit event to audit.jsonl."""
    event = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "event_type": event_type,
        "data": payload
    }
    
    # Sanitize serialized JSON
    raw_str = json.dumps(event)
    sanitized_str = sanitize_text(raw_str)
    
    try:
        with open(LOG_FILE, "a", encoding="utf-8") as f:
            f.write(sanitized_str + "\n")
    except Exception as e:
        # Fallback console print if file writing encounters an error
        print(f"[AuditLog Error] {e}")
