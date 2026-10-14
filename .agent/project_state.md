# Project State & Context
**Last Updated:** 2026-10-14 10:35:00 IST

---

## 1. Project Overview

**Qwenton Apex Android** (`ai.qwenton.apex`) is a production-ready, privacy-first mobile AI operations center designed for Android. The architecture consists of:
1. **Native Android Client**: Built with Jetpack Compose & Material 3, strict OKLCH solid color science (`qwenton-ui`), Room SQLite offline persistence, push-to-talk voice input, Text-to-Speech playback, and Shizuku/ADB Android shell capabilities.
2. **Local Python Execution Server**: Running inside Termux on loopback (`127.0.0.1:8765`), hosting a FastAPI server, multi-model AI routing (Gemini, OpenAI, Claude, Local GGUF via `llama.cpp`), autonomous coding engine, SAST pentesting scanner, and wireless ADB automation tools.
3. **Deterministic Security Layer**: Enforcing cryptographic HMAC-SHA256 signatures, single-use approval nonces, secret redaction, strict path jailing, and zero raw shell execution (`shell=True` strictly forbidden).

---

## 2. Completed Architecture (v2.0 HUD, Key Vault & Coding Engine)

- **Qwenton Futuristic HUD UI (Strict OKLCH Solid Science)**:
  - `QwentonReactorCore.kt`: Dynamic Compose Canvas component with state-reactive concentric rotating rings and breathing pulse (*IDLE*, *LISTENING*, *THINKING*, *EXECUTING*, *APPROVAL_REQUIRED*).
  - `TelemetryBanner.kt`: Real-time telemetry chip bar displaying loopback status, latency, battery %, temperature, active model, and active workspace path.
  - `QuickCommandRail.kt`: Instant one-tap operational carousel (`[📂 Select Folder]`, `[⚡ Run Pentest]`, `[📱 ADB Device Status]`, `[🔍 Inspect Git]`).
  - `CodeDiffCard.kt`: Syntax-highlighted monospace terminal card displaying surgical code additions (`+` emerald) and deletions (`-` crimson).
  - `CommandApprovalDialog.kt`: High-tech modal with HMAC-SHA256 seal badge and interactive code diff preview.
- **In-App AI Engine & Key Vault**:
  - `SettingsScreen.kt` Key Vault card: Segmented provider selector (Gemini, OpenAI, Anthropic, Local GGUF, Custom), password-masked API key field, model ID field, and live save button.
  - `termux-server/app/routes_config.py`: Loopback endpoints (`GET /config/ai-provider`, `POST /config/ai-provider`) writing safely to `.env` and updating in-memory models dynamically.
- **Workspace Selector & Autonomous Coding Suite**:
  - `WorkspaceDialog.kt`: Project directory switcher supporting Termux home presets and custom SD card paths.
  - `termux-server/app/tools/coding.py`: Directory tree (`workspace_tree`), paginated code reader (`read_code_file`), safe file writer (`write_code_file`), and surgical patcher (`patch_code_file`).
- **Autonomous Pentest & Security Suite**:
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
| **In-App Key Vault** | ✅ Completed | `test_ai_config_api` in `test_v2_features.py` |
| **Autonomous Coding Tools** | ✅ Completed | `test_coding_tools_lifecycle` in `test_v2_features.py` |
| **SAST Pentest Engine** | ✅ Completed | `test_pentest_sast_scanner` in `test_v2_features.py` |
| **Shizuku / ADB Bridge** | ✅ Completed | `test_adb_bridge_vectors` in `test_v2_features.py` |
| **Security Audit (OWASP/CWE)**| ✅ 9/9 Patched | Full audit passed; HMAC integrity and path jailing enforced |

---

## 4. Pending Work & Next Steps

1. Launch and compile the app on device via `scripts/deploy_device.bat` or `./scripts/deploy_device.sh`.
2. Authorize Shizuku permissions in the Shizuku manager app on the device.
3. Open Settings in the app, select your preferred AI provider (e.g. Gemini or OpenAI), paste your key into the Key Vault, and begin autonomous coding and phone automation!
