from typing import Any
from app.models import ToolRequestResponse, RiskLevel
from app.tools.battery import execute_get_battery
from app.tools.time_info import execute_get_time
from app.tools.files import execute_list_home, execute_read_allowed_file
from app.tools.coding import workspace_tree, read_code_file, write_code_file, patch_code_file
from app.tools.pentest import run_sast_scan, run_port_audit

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

    elif tool_name == "workspace_tree":
        target_path = arguments.get("path", ".")
        base_dir = arguments.get("base_dir")
        result = workspace_tree(target_path=target_path, base_dir=base_dir)
        return {"tool": tool_name, "result": result}

    elif tool_name == "read_code_file":
        target_path = arguments.get("path", "")
        start_line = int(arguments.get("start_line", 1))
        end_line = int(arguments.get("end_line", 200))
        base_dir = arguments.get("base_dir")
        result = read_code_file(target_path=target_path, base_dir=base_dir, start_line=start_line, end_line=end_line)
        return {"tool": tool_name, "result": result}

    elif tool_name == "write_code_file":
        target_path = arguments.get("path", "")
        content = arguments.get("content", "")
        overwrite = bool(arguments.get("overwrite", False))
        base_dir = arguments.get("base_dir")
        result = write_code_file(target_path=target_path, content=content, base_dir=base_dir, overwrite=overwrite)
        return {"tool": tool_name, "result": result}

    elif tool_name == "patch_code_file":
        target_path = arguments.get("path", "")
        search_chunk = arguments.get("search_chunk", "")
        replace_chunk = arguments.get("replace_chunk", "")
        base_dir = arguments.get("base_dir")
        result = patch_code_file(target_path=target_path, search_chunk=search_chunk, replace_chunk=replace_chunk, base_dir=base_dir)
        return {"tool": tool_name, "result": result}

    elif tool_name == "run_sast_scan":
        target_path = arguments.get("path", ".")
        base_dir = arguments.get("base_dir")
        result = run_sast_scan(project_path=target_path, base_dir=base_dir)
        return {"tool": tool_name, "result": result}

    elif tool_name == "run_port_audit":
        host = arguments.get("host", "127.0.0.1")
        result = run_port_audit(host=host)
        return {"tool": tool_name, "result": result}

    else:
        raise ValueError(f"Unknown or unhandled tool: '{tool_name}'")
