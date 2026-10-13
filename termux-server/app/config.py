import os
from pathlib import Path
from dotenv import load_dotenv

# Load .env file if available
env_path = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(dotenv_path=env_path)

HOST: str = os.getenv("HOST", "127.0.0.1")
PORT: int = int(os.getenv("PORT", "8765"))
DEBUG: bool = os.getenv("DEBUG", "false").lower() == "true"

# Default Termux home or user home
TERMUX_HOME: str = os.getenv("HOME", "/data/data/com.termux/files/home")
ALLOWED_DIRS_RAW: str = os.getenv("ALLOWED_DIRECTORIES", TERMUX_HOME)
ALLOWED_DIRECTORIES: list[str] = [d.strip() for d in ALLOWED_DIRS_RAW.split(",") if d.strip()]

# AI Provider configuration
AI_PROVIDER: str = os.getenv("AI_PROVIDER", "mock")
GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", "")
OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
ANTHROPIC_API_KEY: str = os.getenv("ANTHROPIC_API_KEY", "")
LOCAL_LLM_URL: str = os.getenv("LOCAL_LLM_URL", "http://127.0.0.1:8080/v1")

# Limits
MAX_STDOUT_BYTES: int = 512 * 1024  # 512 KB
COMMAND_TIMEOUT_SECONDS: int = 30
APPROVAL_TIMEOUT_SECONDS: int = 120
