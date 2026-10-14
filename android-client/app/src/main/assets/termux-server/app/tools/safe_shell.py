import shutil
from app.command_policy import is_path_allowed, BLOCKED_PROGRAMS

def validate_program_executable(program: str) -> str:
    """Ensure executable is valid, not blocked, and resolvable in PATH."""
    prog_lower = program.lower()
    if prog_lower in BLOCKED_PROGRAMS:
        raise ValueError(f"Program '{program}' is explicitly blocked.")
    resolved = shutil.which(program)
    if not resolved:
        raise FileNotFoundError(f"Program '{program}' not found on system PATH.")
    return resolved
