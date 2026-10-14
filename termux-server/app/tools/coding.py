import os
from pathlib import Path
from app import config
from app.errors import ToolError

IGNORED_NAMES = {
    ".git", ".gradle", "build", "node_modules", "__pycache__",
    ".idea", "venv", ".venv", ".pytest_cache", ".DS_Store"
}

def _resolve_safe_path(target_path: str, base_dir: str | None = None) -> Path:
    base = Path(base_dir or config.TERMUX_HOME).expanduser().resolve()
    target = (base / target_path).resolve()
    
    # Symlink dereferencing check to prevent jailbreak
    if target.is_symlink():
        target = target.resolve()
        
    try:
        target.relative_to(base)
    except ValueError:
        # Check against allowed directories list
        allowed = False
        for allowed_str in config.ALLOWED_DIRECTORIES:
            allowed_base = Path(allowed_str).expanduser().resolve()
            try:
                target.relative_to(allowed_base)
                allowed = True
                break
            except ValueError:
                continue
        if not allowed:
            raise ToolError(f"Access denied: path '{target_path}' resolves outside allowed workspace: {base}")
            
    return target

def workspace_tree(target_path: str = ".", base_dir: str | None = None, max_depth: int = 3) -> str:
    """Generate a clean directory tree of the workspace."""
    root_dir = _resolve_safe_path(target_path, base_dir)
    if not root_dir.exists() or not root_dir.is_dir():
        raise ToolError(f"Directory not found: {target_path}")

    lines = [f"[Workspace: {root_dir.name}]"]

    def _walk(current: Path, prefix: str, depth: int):
        if depth > max_depth:
            lines.append(f"{prefix}... (depth limit reached)")
            return
        try:
            entries = sorted(list(current.iterdir()), key=lambda e: (not e.is_dir(), e.name.lower()))
        except PermissionError:
            lines.append(f"{prefix}[Permission Denied]")
            return

        filtered = [e for e in entries if e.name not in IGNORED_NAMES and not e.name.endswith(".pyc")]
        for idx, entry in enumerate(filtered):
            is_last = (idx == len(filtered) - 1)
            connector = "└── " if is_last else "├── "
            extension = "/" if entry.is_dir() else ""
            lines.append(f"{prefix}{connector}{entry.name}{extension}")
            if entry.is_dir():
                sub_prefix = prefix + ("    " if is_last else "│   ")
                _walk(entry, sub_prefix, depth + 1)

    _walk(root_dir, "", 1)
    return "\n".join(lines)

def read_code_file(target_path: str, base_dir: str | None = None, start_line: int = 1, end_line: int = 200) -> str:
    """Read lines from a code file with line numbers."""
    file_path = _resolve_safe_path(target_path, base_dir)
    if not file_path.exists():
        raise ToolError(f"File not found: {target_path}")
    if not file_path.is_file():
        raise ToolError(f"Not a file: {target_path}")

    try:
        with open(file_path, "r", encoding="utf-8", errors="replace") as f:
            all_lines = f.readlines()
    except Exception as e:
        raise ToolError(f"Could not read file: {str(e)}")

    total_lines = len(all_lines)
    start_idx = max(1, start_line) - 1
    end_idx = min(total_lines, end_line)

    output = [f"--- File: {target_path} (Lines {start_idx + 1}-{end_idx} of {total_lines}) ---"]
    for i in range(start_idx, end_idx):
        output.append(f"{i + 1:4d} | {all_lines[i].rstrip()}")
    return "\n".join(output)

def write_code_file(target_path: str, content: str, base_dir: str | None = None, overwrite: bool = False) -> str:
    """Create or overwrite a file with given content."""
    file_path = _resolve_safe_path(target_path, base_dir)
    if file_path.exists() and not overwrite:
        raise ToolError(f"File already exists: {target_path}. Specify overwrite=True to replace.")

    try:
        file_path.parent.mkdir(parents=True, exist_ok=True)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)
        return f"Successfully wrote {len(content)} bytes to {target_path}"
    except Exception as e:
        raise ToolError(f"Failed to write file: {str(e)}")

def patch_code_file(target_path: str, search_chunk: str, replace_chunk: str, base_dir: str | None = None) -> str:
    """Perform surgical search-and-replace modification on a file."""
    file_path = _resolve_safe_path(target_path, base_dir)
    if not file_path.exists():
        raise ToolError(f"File not found: {target_path}")

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    occurrences = content.count(search_chunk)
    if occurrences == 0:
        raise ToolError(f"Search chunk not found in {target_path}. Verify line numbers and whitespace.")
    if occurrences > 1:
        raise ToolError(f"Search chunk is ambiguous ({occurrences} occurrences found in {target_path}). Provide more surrounding context.")

    new_content = content.replace(search_chunk, replace_chunk, 1)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)

    return f"Surgical patch applied successfully to {target_path}."
