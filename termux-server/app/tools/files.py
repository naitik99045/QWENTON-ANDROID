import os
from pathlib import Path
from app import config
from app.command_policy import is_path_allowed

async def execute_list_home() -> dict[str, any]:
    """Safely list entries in the Termux home directory."""
    home_dir = config.TERMUX_HOME
    if not os.path.exists(home_dir):
        # Fallback to local cwd if running in dev environment
        home_dir = os.getcwd()

    entries = []
    try:
        for entry in os.scandir(home_dir):
            entries.append({
                "name": entry.name,
                "is_dir": entry.is_dir(),
                "size_bytes": entry.stat().st_size if entry.is_file() else 0
            })
    except Exception as e:
        return {"error": str(e), "entries": []}

    return {
        "directory": home_dir,
        "count": len(entries),
        "entries": entries[:50]  # Return first 50 entries
    }

async def execute_read_allowed_file(file_path: str) -> dict[str, any]:
    """Safely read the content of an allowlisted text file."""
    # Defensively resolve symbolic links before policy check
    resolved = Path(file_path).resolve()

    if not is_path_allowed(str(resolved)):
        raise PermissionError(f"Access to path '{file_path}' (resolving to '{resolved}') is denied by security policy.")

    if not resolved.is_file():
        raise FileNotFoundError(f"File '{file_path}' does not exist.")

    if resolved.stat().st_size > config.MAX_STDOUT_BYTES:
        raise ValueError("File exceeds maximum allowed preview size (512 KB).")

    with open(resolved, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    return {
        "path": str(resolved),
        "size_bytes": len(content),
        "content": content
    }
