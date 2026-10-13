# Qwenton Apex Local Server (Termux)

Python 3 / FastAPI local server running on Android inside Termux, powering **Qwenton Apex**.

## Overview
- **Host**: `127.0.0.1` (Local loopback only)
- **Port**: `8765`
- **Security**: Strictly typed tool allowlist, HMAC-SHA256 integrity verification, no `shell=True`, automatic secret redaction.

## Installation in Termux

```bash
# 1. Update Termux packages and install Python
pkg update && pkg install -y python git termux-api

# 2. Clone or copy project into ~/qwenton-apex-server/
cd ~/qwenton-apex-server

# 3. Create virtual environment & install requirements
python3 -m venv venv
./venv/bin/pip install --upgrade pip
./venv/bin/pip install -r requirements.txt

# 4. Configure environment
cp .env.example .env
```

## Running the Server

```bash
# Start directly or via startup script
chmod +x start.sh
./start.sh
```

Or run manually:
```bash
./venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8765
```

## Running Tests

```bash
./venv/bin/pytest tests/
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Health check probe |
| `POST` | `/chat` | Chat request dispatch & tool proposal |
| `POST` | `/commands/{id}/approve` | User approval & verified execution |
| `POST` | `/commands/{id}/cancel` | Proposal rejection/cancellation |
| `GET` | `/history` | Read audit event history |
| `WS` | `/ws/events` | Real-time WebSocket streaming |
