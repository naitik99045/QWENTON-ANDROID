# Changelog

All notable changes to the **Qwenton Apex Android** project are documented here.

## [0.1.0] - Initial Release Architecture - 2026-10-13

### Added
- **Native Android Client (`ai.qwenton.apex`)**:
  - Jetpack Compose with Material 3 dark charcoal & violet accent theme.
  - Core screens: `ChatScreen`, `TerminalOutputScreen`, `SkillsScreen`, `HistoryScreen`, and `SettingsScreen`.
  - `CommandApprovalDialog` displaying executable, arguments, directory, and risk level.
  - Room SQLite database (`QwentonDatabase`, `ConversationDao`, `ExecutionDao`) for offline persistence.
  - Retrofit REST client and OkHttp WebSocket (`/ws/events`) with exponential backoff reconnect.
  - Push-to-Talk (PTT) `SpeechRecognizerManager` and `TextToSpeechManager`.
  - `TermuxServiceHelper` integration using `com.termux.permission.RUN_COMMAND`.

- **Termux Local Python Server (`~/qwenton-apex-server`)**:
  - FastAPI server with endpoints `/health`, `/chat`, `/commands/{id}/approve`, `/commands/{id}/cancel`, `/history`, and `/ws/events`.
  - Deterministic security and policy engine (`command_policy.py`) with strict tool allowlist, path traversal block, and HMAC-SHA256 integrity checks.
  - Sandboxed asynchronous subprocess runner (`command_runner.py`) with stdout/stderr chunk streaming, 512 KB buffer cap, and 30s timeout.
  - Secret scrubber filter (`audit_log.py`) masking API keys and tokens in audit logs.
  - Built-in tools: `get_time`, `get_battery`, `list_home`, `read_allowed_file`.
  - Pytest test suite covering health, API contracts, policy enforcement, and subprocess execution.

- **Living Context & Documentation**:
  - `.agent/project_state.md` initialized.
  - Architecture, security, setup, and troubleshooting documentation in `docs/`.

## [0.1.1] - Security Hardening & Penetration Testing Remediation - 2026-10-13

### Security
- **Android Manifest & Network Security**:
  - Disabled ADB backup data extraction (`android:allowBackup="false"`).
  - Enforced `network_security_config.xml` restricting cleartext HTTP exclusively to `127.0.0.1` and `localhost`.
- **Termux Server Sandbox & Policy**:
  - Fixed symlink traversal vulnerability in file inspection tool (`files.py`) by resolving canonical target before allowlist check.
  - Hardened HMAC integrity hash calculation with deterministic JSON serialization (`command_policy.py`).
  - Switched allowlist path normalization to `os.path.realpath` to prevent symlink traversal.
  - Added atomic `PENDING` state locking in `routes_commands.py` to prevent concurrent double-execution race conditions.
  - Added 32 KB chat payload bounds in `routes_chat.py` to prevent memory exhaustion DoS.
  - Restricted CORS and WebSocket origins to local loopback origins to prevent cross-origin browser CSRF.
  - Added URL validation and fallback in Android `NetworkModule.kt` to prevent Retrofit runtime crashes.

## [0.2.0] - On-Device Termux & Shizuku ADB Autonomous Automation - 2026-10-14

### Added
- **On-Device Server Bundling & Extraction**:
  - Embedded Termux server source bundle into Android APK assets (`assets/termux-server/`).
  - Added in-app `SetupWizardDialog` with 1-tap extraction, dependency installer script, and step-by-step guidance.
- **Custom Provider & Open-Weights AI Endpoint Support**:
  - Configurable `base_url` + `api_key` support across FastAPI server (`config.py`, `ai_client.py`, `routes_config.py`).
  - Synced Settings and Setup Wizard in Jetpack Compose UI allowing users to point to any OpenAI-compatible provider (Ollama, vLLM, OpenRouter, DeepSeek, LocalAI, etc.).
- **Shizuku & Android Native Device Automation**:
  - Shizuku wireless ADB integration for terminal commands without a PC.
  - Device automation intents for opening apps, dialer calls, camera, and notification access.

## [0.2.1] - Git & Repository Hygiene for GitHub Release - 2026-10-14

### Changed
- Hardened root and subproject `.gitignore` files to guarantee sensitive credentials (`.env`, keystores, private keys, audit logs) are never committed to GitHub.
- Configured explicit whitelist exceptions for `.env.example` templates and APK offline extraction payload (`assets/termux-server/`).

