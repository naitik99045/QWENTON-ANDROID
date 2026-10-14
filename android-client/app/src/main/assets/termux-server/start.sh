#!/data/data/com.termux/files/usr/bin/bash
set -e

# Qwenton Apex Local Server Startup Script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Starting Qwenton Apex Local Server ==="

# Check Python3 installation
if ! command -v python3 &> /dev/null; then
    echo "Python3 is not installed. Run: pkg install python"
    exit 1
fi

# Set up virtual environment if absent
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
    ./venv/bin/pip install --upgrade pip
    ./venv/bin/pip install -r requirements.txt
fi

# Load environment
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

PORT="${PORT:-8765}"
HOST="${HOST:-127.0.0.1}"

echo "Starting server on http://${HOST}:${PORT}..."
exec ./venv/bin/uvicorn app.main:app --host "$HOST" --port "$PORT"
