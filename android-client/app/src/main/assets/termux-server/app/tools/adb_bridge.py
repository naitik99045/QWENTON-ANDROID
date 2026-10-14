import shutil
from app.errors import ToolError

def is_adb_installed() -> bool:
    """Check if adb executable is available in PATH."""
    return shutil.which("adb") is not None

def build_adb_device_status_args() -> list[str]:
    """Inspect connected ADB device status."""
    return ["adb", "devices", "-l"]

def build_adb_list_packages_args(third_party_only: bool = True) -> list[str]:
    """List installed application packages via ADB package manager."""
    args = ["adb", "shell", "pm", "list", "packages"]
    if third_party_only:
        args.append("-3")
    return args

def build_adb_launch_app_args(package_name: str, activity_name: str | None = None) -> list[str]:
    """Launch an application or activity via ADB activity manager."""
    if not package_name.replace(".", "").replace("_", "").isalnum():
        raise ToolError(f"Invalid package identifier: {package_name}")

    if activity_name:
        target = f"{package_name}/{activity_name}"
        return ["adb", "shell", "am", "start", "-n", target]
    else:
        return ["adb", "shell", "monkey", "-p", package_name, "-c", "android.intent.category.LAUNCHER", "1"]

def build_adb_tap_args(x: int, y: int) -> list[str]:
    """Simulate a screen tap at (x, y) coordinates."""
    if x < 0 or y < 0:
        raise ToolError("Tap coordinates must be positive integers.")
    return ["adb", "shell", "input", "tap", str(x), str(y)]

def build_adb_text_args(text: str) -> list[str]:
    """Simulate typing text via ADB keyboard input."""
    # Sanitize text to prevent unexpected control characters
    sanitized = text.replace(" ", "%s").replace("'", "")
    return ["adb", "shell", "input", "text", sanitized]

def build_adb_key_args(key_code: int) -> list[str]:
    """Simulate hardware key events (e.g. 3=Home, 4=Back, 26=Power)."""
    return ["adb", "shell", "input", "keyevent", str(key_code)]

def build_adb_screencap_args(output_path: str = "/sdcard/Download/qwenton_screen.png") -> list[str]:
    """Capture screen image via ADB screencap utility."""
    return ["adb", "shell", "screencap", "-p", output_path]
