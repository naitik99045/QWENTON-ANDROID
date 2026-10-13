from typing import Any
from app.models import ToolRequestResponse, RiskLevel
from app.tools.battery import execute_get_battery
from app.tools.time_info import execute_get_time
from app.tools.files import execute_list_home, execute_read_allowed_file
from app.command_policy import create_command_proposal

async def route_and_execute_tool(
    request_id: str,
    tool_name: str,
    arguments: dict[str, Any]
) -> dict[str, Any]:
    """Execute a registered safe tool or raise appropriate errors."""
    if tool_name == "get_time":
        result = await execute_get_time()
        return {"tool": tool_name, "result": result}

    elif tool_name == "get_battery":
        result = await execute_get_battery()
        return {"tool": tool_name, "result": result}

    elif tool_name == "list_home":
        result = await execute_list_home()
        return {"tool": tool_name, "result": result}

    elif tool_name == "read_allowed_file":
        path = arguments.get("path", "")
        result = await execute_read_allowed_file(path)
        return {"tool": tool_name, "result": result}

    else:
        raise ValueError(f"Unknown or unhandled tool: '{tool_name}'")
