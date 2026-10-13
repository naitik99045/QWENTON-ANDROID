# Setting up Qwenton Apex Local Server on Termux

## Prerequisites
1. Install Termux from [F-Droid](https://f-droid.org/en/packages/com.termux/) (do not use Google Play versions).
2. Install Termux:API from F-Droid to grant hardware inspection capabilities (battery, notifications, sensors).

## Step-by-Step Installation

### 1. Update Termux Environment
```bash
pkg update && pkg upgrade -y
pkg install -y python git termux-api
```

### 2. Scaffold Server Directory
```bash
mkdir -p ~/qwenton-apex-server
cd ~/qwenton-apex-server
```

Copy the `termux-server` contents into `~/qwenton-apex-server/`.

### 3. Initialize Python Virtual Environment
```bash
python3 -m venv venv
./venv/bin/pip install --upgrade pip
./venv/bin/pip install -r requirements.txt
```

### 4. Configure Environment Secrets
```bash
cp .env.example .env
nano .env
```
Optional: Set `GEMINI_API_KEY` or `OPENAI_API_KEY` if remote model reasoning is desired.

### 5. Start Server
```bash
chmod +x start.sh
./start.sh
```
The server will bind strictly to `127.0.0.1:8765`.

### 6. Enable Android RUN_COMMAND Permission
In Termux, create or update `~/.termux/termux.properties`:
```properties
allow-external-apps = true
```
Then restart Termux:
```bash
termux-reload-settings
```
This allows the Qwenton Apex Android client to launch `start.sh` via the `RUN_COMMAND` intent.
