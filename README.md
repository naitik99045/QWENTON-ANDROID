# Qwenton Apex Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.111-green.svg)](https://fastapi.tiangolo.com)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Termux-darkblue.svg)](https://termux.dev)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

**Qwenton Apex Android** (`ai.qwenton.apex`) is a production-ready, privacy-first mobile AI operations center for Android devices. It pairs a native Jetpack Compose application (featuring an animated Reactor Core HUD, in-app Key Vault, app launcher, contact dialing, and wake-word engine) with a local Python 3 / FastAPI server executing inside Termux on loopback (`127.0.0.1:8765`).

---

## 🏛️ System Architecture

```text
+-----------------------------------------------------------------------------------------+
|                                     Android Device                                      |
|                                                                                         |
|  [Android Native Client] (ai.qwenton.apex)            [Termux Environment]              |
|   - Qwenton Reactor Core HUD (Compose Canvas)          Directory: ~/qwenton-apex-server |
|   - In-App AI Key Vault (Gemini / OpenAI / GGUF)       - Python 3 + FastAPI             |
|   - Universal App Launcher (Spotify, WhatsApp)         - Deterministic Policy Engine    |
|   - Contact Search & Phone Calling (Safe Gate)         - HMAC-SHA256 Integrity Hasher   |
|   - Notification Listener (ADB Auto-Pairing)           - Autonomous Coding Engine       |
|   - Hands-Free Wake-Word ("Hey Qwenton")               - SAST Pentest Security Suite    |
|   - Shizuku Rootless Shell Binder (UID 2000)           - Wireless ADB Bridge            |
|   - Room DB (Offline History) & OkHttp WebSockets      - Termux:API Bridge              |
|                                                                                         |
|            ▲                                                     ▲                      |
|            │                                                     │                      |
|            └──────────── HTTP / WebSocket (/ws/events) ──────────┘                      |
|                               (Loopback: 127.0.0.1:8765)                                |
+-----------------------------------------------------------------------------------------+
```

---

## ✨ Features & Capabilities

1. **Qwenton Reactor Core HUD**: Dynamic Canvas-drawn animated orbital core displaying real-time AI states (*Idle*, *Listening*, *Thinking*, *Executing*, *Approval Required*), live latency, battery, and workspace telemetry. Built strictly using `qwenton-ui` solid OKLCH dark color tokens (zero gradients).
2. **In-App AI Engine & Key Vault**: Configure Gemini, OpenAI, Claude, Local GGUF (`llama.cpp`), or **any custom online provider** (OpenRouter, Groq, DeepSeek, Together, Ollama, vLLM) by simply entering its `Base URL` + `API Key` + `Model ID`. Keys are encrypted and synced to Termux over loopback without manual terminal editing.
3. **Universal App Launcher**: Open any installed app on your phone with natural commands like *"open spotify"*, *"open youtube"*, *"launch settings"*.
4. **Contact Search & Voice Calling**: Search address book contacts with parameterized SQL-injection-safe queries and place calls via an explicit authorization modal.
5. **Wireless Debugging Auto-Pairing**: Built-in `NotificationListenerService` automatically extracts pairing codes and ports from Developer Options notifications to auto-pair Shizuku/ADB in 1 second.
6. **Hands-Free Wake-Word**: Background foreground service continuously listening for *"Hey Qwenton"* or *"Qwenton"*.
7. **Autonomous Coding & Pentesting Engine**: Workspace folder selector, directory tree visualizer, paginated code reader, surgical patcher (`patch_file`), and automated SAST vulnerability scanner.
8. **Shizuku & ADB Phone Control**: Simulate screen taps (`input tap x y`), type text, press Home/Back keys, and inspect system state with rootless UID 2000 shell privileges.

---

## 📱 100% On-Device Standalone Setup (Zero PC / Laptop Required)

Qwenton Apex is engineered to run **100% standalone on an Android phone** without needing a computer, PC, or laptop:

1. **Install the APK** on your phone (downloaded from releases or built with Gradle).
2. **Open Qwenton Apex**: The interactive **3-Step On-Device Setup Wizard** opens automatically (or tap `[🚀 Setup Wizard (No PC)]` from the HUD Quick Rail).
3. **Step 1 — Termux Server**:
   * Tap **`1-Tap Copy & Setup`**: Qwenton extracts the full local server payload to `/sdcard/Download/qwenton-apex-server` and copies the universal setup command.
   * Tap **`Open Termux`**: Long-press inside Termux, select **Paste**, and press Enter. The script installs Python, creates virtualenv, installs dependencies, and boots `http://127.0.0.1:8765`.
   * Return to Qwenton Apex: The status indicator immediately turns green `[SYNC]`.
4. **Step 2 — Shizuku & Wireless ADB**:
   * Tap **`Enable Notification Access`**: Grants permission so Qwenton can auto-intercept the 6-digit wireless debugging PIN.
   * Tap **`Open Developer Options`**: Enable *Wireless Debugging* -> *Pair device with pairing code*. Qwenton automatically intercepts the PIN and pairs your shell in 1 second!
5. **Step 3 — In-App AI Key Vault & Custom Provider**:
   * Select your AI Provider (**Google Gemini**, **OpenAI**, **Custom / Online**, or **Local GGUF**).
   * For **Custom Provider**: Enter your provider's `Base URL` (e.g. `https://openrouter.ai/api/v1`, `https://api.groq.com/openai/v1`, `https://api.deepseek.com/v1`), your API Key, and your target model (e.g. `deepseek/deepseek-chat`).
   * Tap **`Save to Key Vault`**: All settings are safely saved directly into the local Termux `.env` without typing secrets in a terminal.

---

## 💻 Optional: 1-Click Deployment from PC / Laptop (Developers Only)

If you have a PC or laptop and prefer pushing via ADB:

### Windows:
```cmd
scripts\deploy_device.bat
```

### Linux / macOS:
```bash
chmod +x scripts/deploy_device.sh
./scripts/deploy_device.sh
```

---

## 🛡️ Defensive Security Certification (OWASP / CWE)

* **Zero Raw Shells**: `shell=True` is strictly prohibited. Subprocesses run with fixed program names and argument vectors.
* **HMAC-SHA256 Cryptographic Seals**: Every proposed command is hashed with single-use nonces and 120-second expiration.
* **Notification Privacy**: Non-pairing notifications are completely discarded to protect private messages.
* **SQL Injection Immune**: Contact queries use parameterized selection arguments.
* **Toll Fraud Safeguard**: Autonomous dialing is blocked; calls require explicit user confirmation.
* **Audit Trail**: All events are logged with masked secrets (`AIza...****`).

Full audit report: [`.agent/security_report.md`](.agent/security_report.md)
