#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "============================================================"
echo "  Qwenton Apex: 100% On-Device Standalone Setup"
echo "  (No PC or Laptop Required)"
echo "============================================================"
echo ""

# 1. Install prerequisites
echo "[1/4] Updating package repos & installing dependencies..."
pkg update -y
pkg install -y python git termux-api android-tools

# 2. Locate and deploy server payload
echo "[2/4] Deploying server payload to ~/qwenton-apex-server..."
mkdir -p ~/qwenton-apex-server

if [ -d "/sdcard/Download/qwenton-apex-server" ]; then
    echo "Found exported app server payload in /sdcard/Download, copying..."
    cp -r /sdcard/Download/qwenton-apex-server/* ~/qwenton-apex-server/
elif [ -d "/sdcard/qwenton-apex-server" ]; then
    echo "Found storage server payload in /sdcard, copying..."
    cp -r /sdcard/qwenton-apex-server/* ~/qwenton-apex-server/
else
    echo "Payload not found in external storage, checking Termux home..."
fi

cd ~/qwenton-apex-server

# 3. Create Python virtual environment
echo "[3/4] Initializing Python virtual environment..."
if [ ! -d "venv" ]; then
    python3 -m venv venv
    ./venv/bin/pip install --upgrade pip
fi

if [ -f "requirements.txt" ]; then
    ./venv/bin/pip install -r requirements.txt
else
    ./venv/bin/pip install fastapi uvicorn httpx pydantic python-dotenv
fi

# 4. Make start script executable and boot server
echo "[4/4] Starting Qwenton Apex Local Server..."
chmod +x start.sh 2>/dev/null || true
if [ -f "start.sh" ]; then
    ./start.sh
else
    ./venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8765
fi
