# Setting up Qwenton Apex Local Server on Termux

## Prerequisites
1. Install Termux from [F-Droid](https://f-droid.org/en/packages/com.termux/) (do not use Google Play versions).
2. Install Termux:API from F-Droid to grant hardware inspection capabilities (battery, notifications, sensors).

## 🚀 Method A: 1-Tap On-Device Setup from Qwenton App (Recommended - Zero PC)

1. Open the **Qwenton Apex** app on your phone.
2. Tap the `[🚀 Setup Wizard (No PC)]` chip on the home screen.
3. Tap **"1-Tap Copy & Setup"**:
   * The app extracts the full FastAPI server codebase to `/sdcard/Download/qwenton-apex-server/`.
   * It copies the 1-line bootstrapping script to your Android clipboard.
4. Tap **"Open Termux"**:
   * Long press on the Termux terminal window, tap **Paste**, and press Enter.
   * Termux updates packages, creates the virtual environment, installs dependencies, and boots the server at `127.0.0.1:8765`.
5. Return to Qwenton Apex: The status indicator immediately displays `[SYNC] Connected`.

---

## 🛠️ Method B: Manual Step-by-Step Installation

If you prefer installing manually inside Termux:

### 1. Update Termux Environment
```bash
pkg update && pkg upgrade -y
pkg install -y python git termux-api android-tools
```

### 2. Scaffold Server Directory
```bash
mkdir -p ~/qwenton-apex-server
cd ~/qwenton-apex-server
```

Copy or extract the server payload into `~/qwenton-apex-server/`.

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
Optional: Set `GEMINI_API_KEY`, `OPENAI_API_KEY`, or `CUSTOM_BASE_URL` + `CUSTOM_API_KEY` for OpenRouter / Groq / DeepSeek / Together.

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
