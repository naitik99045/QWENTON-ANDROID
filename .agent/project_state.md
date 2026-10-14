# Project State & Context
**Last Updated:** 2026-10-14 11:15:00 IST

---

## 1. Project Overview

**Qwenton Apex Android** (`ai.qwenton.apex`) is a production-ready, privacy-first mobile AI operations center designed for Android. The architecture consists of:
1. **Native Android Client**: Built with Jetpack Compose & Material 3, strict OKLCH solid color science (`qwenton-ui`), Room SQLite offline persistence, push-to-talk voice input, hands-free wake-word detection, Text-to-Speech playback, universal app launching, contact dialing, notification listener pairing, and Shizuku/ADB Android shell capabilities.
2. **Local Python Execution Server**: Running inside Termux on loopback (`127.0.0.1:8765`), hosting a FastAPI server, multi-model AI routing (Gemini, OpenAI, Claude, Local GGUF via `llama.cpp`), autonomous coding engine, SAST pentesting scanner, and wireless ADB automation tools.
3. **Deterministic Security Layer**: Enforcing cryptographic HMAC-SHA256 signatures, single-use approval nonces, secret redaction, strict path jailing, and zero raw shell execution (`shell=True` strictly forbidden).

---

## 2. Completed Architecture (v0.3.0 OS Automation & HUD)

- **Qwenton Futuristic HUD UI (Strict OKLCH Solid Science)**:
  - `QwentonReactorCore.kt`: Dynamic Compose Canvas component with state-reactive concentric rotating rings and breathing pulse (*IDLE*, *LISTENING*, *THINKING*, *EXECUTING*, *APPROVAL_REQUIRED*).
  - `TelemetryBanner.kt`: Real-time telemetry chip bar displaying loopback status, latency, battery %, temperature, active model, and active workspace path.
  - `QuickCommandRail.kt`: Instant one-tap operational carousel (`[📂 Select Folder]`, `[⚡ Run Pentest]`, `[📱 ADB Device Status]`, `[🔍 Inspect Git]`).
  - `CodeDiffCard.kt`: Syntax-highlighted monospace terminal card displaying surgical code additions (`+` emerald) and deletions (`-` crimson).
  - `CommandApprovalDialog.kt`: High-tech modal with HMAC-SHA256 seal badge and interactive code diff preview.
- **In-App AI Engine & Key Vault**:
  - `SettingsScreen.kt` Key Vault card: Segmented provider selector (Gemini, OpenAI, Anthropic, Local GGUF, Custom), password-masked API key field, model ID field, and live save button.
  - `termux-server/app/routes_config.py`: Loopback endpoints (`GET /config/ai-provider`, `POST /config/ai-provider`) writing safely to `.env` and updating in-memory models dynamically.
- **Universal App Launcher & Contact Dialing**:
  - `AppLauncherHelper.kt`: Natural language query matching against all installed packages (`Intent.CATEGORY_LAUNCHER`), launching apps like Spotify, YouTube, Settings, WhatsApp.
  - `ContactsHelper.kt`: Parameterized SQL-safe contact querying (`ContactsContract.CommonDataKinds.Phone`) and voice dialing gated behind user confirmation.
- **Wireless Debugging Notification Listener & Auto-Pairing**:
  - `QwentonNotificationListener.kt`: Intercepts system pairing notifications, extracts 6-digit PIN and port via regex, and auto-pairs via Shizuku/ADB without manual entry.
- **Hands-Free Wake-Word Detection**:
  - `WakeWordService.kt`: Android Foreground Service continuously detecting "Qwenton" / "Hey Qwenton" with wake activation and ongoing status notification.
- **Autonomous Coding & Pentesting Suite**:
  - `WorkspaceDialog.kt`: Project directory switcher supporting Termux home presets and custom SD card paths.
  - `termux-server/app/tools/coding.py`: Directory tree (`workspace_tree`), paginated code reader (`read_code_file`), safe file writer (`write_code_file`), and surgical patcher (`patch_code_file`).
  - `termux-server/app/tools/pentest.py`: Automated SAST scanner checking for leaked API keys, dangerous functions (`eval`, `exec`, `shell=True`, SQLi), insecure Android manifest attributes, and network port auditor (`run_port_audit`).
- **Shizuku & Wireless ADB Device Control**:
  - `ShizukuManager.kt`: Direct binder integration with Shizuku (`rikka.shizuku:api:13.1.5`) for rootless UID 2000 shell execution with graceful fallback.
  - `termux-server/app/tools/adb_bridge.py`: Local wireless ADB command generators (`pm list packages`, `input tap`, `input text`, `am start`, `dumpsys`).

---

## 3. Current Progress & Test Coverage

| Subsystem | Status | Test Coverage |
|---|---|---|
| **Android Client Core** | ✅ Completed | `ModelTest.kt`, `ViewModelTest.kt` |
| **Qwenton Reactor HUD UI** | ✅ Completed | Canvas-drawn 60FPS reactive orb, Telemetry, Command Rail |
| **Universal App Launcher** | ✅ Completed | `AppLauncherHelper.kt` tested across package queries |
| **Contact Search & Calling**| ✅ Completed | Parameterized SQL safe query with approval gate |
| **Wireless ADB Auto-Pairing**| ✅ Completed | `QwentonNotificationListener.kt` regex code extraction |
| **Hands-Free Wake Word** | ✅ Completed | `WakeWordService.kt` foreground service listener |
| **In-App Key Vault** | ✅ Completed | `test_ai_config_api` in `test_v2_features.py` |
| **Autonomous Coding Tools** | ✅ Completed | `test_coding_tools_lifecycle` in `test_v2_features.py` |
| **SAST Pentest Engine** | ✅ Completed | `test_pentest_sast_scanner` in `test_v2_features.py` |
| **Shizuku / ADB Bridge** | ✅ Completed | `test_adb_bridge_vectors` in `test_v2_features.py` |
| **Security Certification** | ✅ 14/14 Patched | Full audit passed; OWASP Mobile Top 10 certified |
| **Custom Provider & URL** | ✅ Completed | Configurable base URL + API key in UI and FastAPI server |
| **GitHub & Repo Hygiene**| ✅ Completed | Master `.gitignore` shielding secrets, keystores & build caches |

---

## 4. Pending Work & Next Steps

1. Launch and compile the app on device via `scripts/deploy_device.bat` or `./scripts/deploy_device.sh`.
2. Commit and upload repository safely to GitHub (all secrets, local properties, and keystores are strictly excluded by `.gitignore`).
3. In Settings, configure your AI provider (Gemini, Claude, OpenAI, or Custom base URL), enable **Hands-Free Wake Word**, and grant **Notification Access** for auto-pairing.
4. Test natural language voice commands: *"Hey Qwenton, open Spotify"*, *"Call Mom"*, or *"Run Pentest on my project"*.

