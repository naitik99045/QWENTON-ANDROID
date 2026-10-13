# Qwenton Apex Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.111-green.svg)](https://fastapi.tiangolo.com)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Termux-darkblue.svg)](https://termux.dev)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

**Qwenton Apex Android** (`ai.qwenton.apex`) is a privacy-first mobile AI assistant architecture for Android devices. It pairs a native Jetpack Compose application with a local Python 3 / FastAPI server executing inside Termux on loopback (`127.0.0.1:8765`).

---

## 🏛️ System Architecture

```text
+-------------------------------------------------------------------------------+
|                                Android Device                                 |
|                                                                               |
|  [Android Native Client]                     [Termux Environment]             |
|   Package: ai.qwenton.apex                    Directory: ~/qwenton-apex-server |
|   - Jetpack Compose (Material 3 Dark)         - Python 3 + FastAPI             |
|   - ViewModel + StateFlow                     - Deterministic Policy Engine    |
|   - Room DB (Offline History)                 - HMAC-SHA256 Integrity Hasher   |
|   - Push-to-Talk Voice & TTS                  - Safe Subprocess Runner         |
|   - OkHttp WebSockets & Retrofit REST         - Termux:API Bridge              |
|                                                                               |
|            ▲                                           ▲                      |
|            │                                           │                      |
|            └─────── HTTP / WebSocket (/ws/events) ─────┘                      |
|                     (Loopback: 127.0.0.1:8765)                                |
+-------------------------------------------------------------------------------+
```

---

## 📦 Repository Layout

```text
.
├── .agent/                  # Living persistent AI context and state
│   └── project_state.md
├── android-client/          # Native Android Compose client
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle/libs.versions.toml
│   └── app/
│       ├── build.gradle.kts
│       └── src/main/java/ai/qwenton/apex/
│           ├── MainActivity.kt
│           ├── QwentonApp.kt
│           ├── data/       # Room Entities, DAOs, Database
│           ├── domain/     # Models, Repositories, Policies
│           ├── network/    # Retrofit, WebSockets, DTOs
│           ├── ui/         # Chat, Terminal, Skills, History, Settings
│           ├── voice/      # SpeechRecognizer (PTT), TextToSpeech
│           └── service/    # Termux RUN_COMMAND Integration
├── termux-server/           # Local Python server inside Termux
│   ├── app/
│   │   ├── main.py         # FastAPI gateway & WebSocket events
│   │   ├── command_policy.py # Strict allowlists & SHA-256 HMAC verifier
│   │   ├── command_runner.py # Asyncio subprocess execution (no shell=True)
│   │   ├── audit_log.py    # JSONL audit trail with secret scrubbers
│   │   ├── ai_client.py    # Multi-provider LLM abstraction (Mock/Gemini/Local)
│   │   └── tools/          # battery, time_info, files, safe_shell
│   ├── tests/              # Pytest test suite
│   ├── requirements.txt
│   └── start.sh
└── docs/                    # Architecture, security, and setup guides
    ├── architecture.md
    ├── setup-termux.md
    ├── security.md
    ├── troubleshooting.md
    └── api-contract.md
```

---

## 🚀 Quick Start Guide

### 1. Setup & Run the Termux Server
Inside the Termux application on your Android device:
```bash
pkg update && pkg install -y python git termux-api
cd ~/
# Copy or clone repo
cd ~/qwenton-apex-server
python3 -m venv venv
./venv/bin/pip install -r requirements.txt
chmod +x start.sh
./start.sh
```

### 2. Build & Install Android Client
On your development machine:
```bash
cd android-client
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Core Security Model
- **Zero Raw Shells**: Execution using `shell=True` or raw shell strings is prohibited. Fixed program names with explicit argument arrays are enforced.
- **HMAC-SHA256 Integrity Hashes**: Proposals are hashed server-side upon creation. Any client tampering invalidates approval.
- **Approval Expiry**: Proposals expire after 120 seconds.
- **Output Capping**: Subprocess stdout/stderr streams are limited to 512 KB per invocation.
- **Secret Redaction**: Regex filters remove API keys (`sk-...`, `AIza...`, `Bearer ...`) from logs and broadcasts.

For detailed documentation, explore the [`docs/`](docs/) directory.
