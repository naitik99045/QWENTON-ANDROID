import json
import shutil
import asyncio

async def execute_get_battery() -> dict[str, any]:
    """Retrieve battery status using termux-battery-status or development fallback."""
    if shutil.which("termux-battery-status"):
        try:
            proc = await asyncio.create_subprocess_exec(
                "termux-battery-status",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, _ = await asyncio.wait_for(proc.communicate(), timeout=5.0)
            if proc.returncode == 0:
                return json.loads(stdout.decode("utf-8"))
        except Exception:
            pass

    # Safe fallback/simulated output if running on host or emulator
    return {
        "percentage": 88,
        "status": "CHARGING",
        "health": "GOOD",
        "temperature": 29.5,
        "plugged": "PLUGGED_AC"
    }
