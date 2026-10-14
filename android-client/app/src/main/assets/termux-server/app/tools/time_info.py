from datetime import datetime, timezone

async def execute_get_time() -> dict[str, str]:
    """Retrieve current system UTC and local formatted timestamp."""
    utc_now = datetime.now(timezone.utc)
    return {
        "utc_time": utc_now.isoformat(),
        "formatted": utc_now.strftime("%A, %B %d, %Y %I:%M:%S %p UTC")
    }
