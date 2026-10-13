from datetime import datetime, timezone
from fastapi import APIRouter
from app.models import HealthResponse

router = APIRouter()

@router.get("/health", response_model=HealthResponse)
async def health_check():
    """Server health probe."""
    return HealthResponse(
        status="ok",
        service="qwenton-apex",
        version="0.1.0",
        server_time=datetime.now(timezone.utc).isoformat()
    )
