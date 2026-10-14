import json
from pathlib import Path
from fastapi import APIRouter
from app.audit_log import LOG_FILE

router = APIRouter()

@router.get("/history")
async def get_history():
    """Retrieve audit and execution log history."""
    if not LOG_FILE.exists():
        return {"events": []}

    events = []
    with open(LOG_FILE, "r", encoding="utf-8") as f:
        for line in f:
            if line.strip():
                try:
                    events.append(json.loads(line))
                except Exception:
                    pass
    return {"events": events[-100:]}  # Return latest 100 events

@router.delete("/history/{item_id}")
async def delete_history_item(item_id: str):
    """Acknowledge item deletion."""
    return {"status": "deleted", "id": item_id}
