# Qwenton Apex Implementation Roadmap

## Phase 1: Context & Scaffolding
- [x] Analyze PRD & master specifications (`prompt.md`)
- [x] Initialize `.agent/project_state.md` living context
- [x] Scaffold Android client with Gradle version catalog & Material 3 Dark theme
- [x] Scaffold Termux Python server with FastAPI, Pydantic, and security policy
- [x] Document architecture, security, setup, and troubleshooting in `docs/`

## Phase 2: Android Core Integration & ViewModel Wiring
- [x] Implement Room database entities, DAOs, and `QwentonDatabase`
- [x] Implement Retrofit HTTP client (`QwentonApi`) and OkHttp `WebSocketClient`
- [x] Implement `QwentonRepository` and `QwentonViewModel`
- [x] Create `QwentonViewModelFactory` for lifecycle-aware dependency injection
- [x] Connect `MainActivity.kt` directly to `QwentonViewModel`
- [x] Integrate `SpeechRecognizerManager` with Push-to-Talk button in `ChatScreen`
- [x] Integrate `TextToSpeechManager` with incoming assistant message stream
- [x] Connect `TermuxServiceHelper` to Settings screen "Start/Restart Server" button

## Phase 3: Termux Server Policy Hardening & Tools
- [x] Implement `command_policy.py` with HMAC-SHA256 integrity verification
- [x] Implement `command_runner.py` with argument vector execution & buffer capping
- [x] Implement secret redaction filter in `audit_log.py`
- [x] Implement safe tools: `battery`, `time_info`, `files`, `safe_shell`
- [x] Implement startup script `start.sh` with virtualenv bootstrapping
- [x] Create Pytest test suite for health, policy, runner, and API contracts

## Phase 4: Security Pentest & Integrity Certification
- [x] Perform static vulnerability analysis (SAST) on Android and Python code
- [x] Verify zero hardcoded secrets or sensitive credentials
- [x] Generate `.agent/security_report.md`

## Phase 5: Production Readiness, Build Verification & Hardware Deployment
- [x] Generate Gradle wrapper scripts (`gradlew`, `gradlew.bat`, `gradle-wrapper.properties`)
- [x] Create adaptive vector launcher icons (`ic_launcher.xml`, `ic_launcher_round.xml`) to prevent AAPT2 link errors
- [x] Write `ViewModelTest.kt` unit test suite
- [x] Write `conftest.py` shared pytest fixtures for Termux server tests
- [x] Add automated ADB device deployment scripts (`deploy_device.bat`, `deploy_device.sh`)
- [x] Add Local GGUF / llama.cpp offline model integration guide (`docs/local-llm-setup.md`)
- [x] Update `ai_client.py` with local offline model provider support
- [x] Create complete walkthrough documentation (`walkthrough.md`)

## Phase 6: Penetration Testing & Vulnerability Remediation (OWASP / CWE)
- [x] [SEC-01] Disable insecure ADB backup extraction (`allowBackup="false"`)
- [x] [SEC-02] Restrict cleartext HTTP to 127.0.0.1 via `network_security_config.xml`
- [x] [SEC-03] Fix TOCTOU symlink traversal in `files.py` (resolve target before check)
- [x] [SEC-04] Fix ambiguous argument serialization in HMAC integrity hash (`command_policy.py`)
- [x] [SEC-05] Replace lexical `abspath` with `realpath` to prevent symlink traversal
- [x] [SEC-06] Guard against command approval replay & race conditions in `routes_commands.py`
- [x] [SEC-07] Enforce 32KB payload size limits in `routes_chat.py` to prevent DoS
- [x] [SEC-08] Restrict CORS & WebSocket origins to local loopback to prevent browser CSRF
- [x] [SEC-09] Validate and sanitize custom server URLs in `NetworkModule.kt` to prevent app crashes

## Phase 7: v2.0 Qwenton Reactor HUD, Key Vault, Coding Engine & Shizuku/ADB Automation
- [x] Implement animated Qwenton Reactor Core with Canvas state animations (`QwentonReactorCore.kt`)
- [x] Implement real-time Telemetry Banner (`TelemetryBanner.kt`) with latency, battery, and engine readouts
- [x] Implement Quick Command Rail (`QuickCommandRail.kt`) with instant action triggers
- [x] Implement Code Diff Card with surgical diff syntax highlighting (`CodeDiffCard.kt`)
- [x] Overhaul Chat Screen and Color palette adhering strictly to `qwenton-ui` solid OKLCH standards
- [x] Build In-App AI Key Vault & Server Config Sync (`routes_config.py`, `SettingsScreen.kt`, and `QwentonViewModel.kt`)
- [x] Build Workspace Folder Selector dialog (`WorkspaceDialog.kt`) for agile directory switching
- [x] Build Autonomous Coding Toolset (`tools/coding.py`: tree, read, write, surgical patch)
- [x] Build Automated SAST Pentesting Suite (`tools/pentest.py`: leaked keys, dangerous calls, port audit)
- [x] Integrate Shizuku Rootless Shell Binder (`ShizukuManager.kt`) & Termux Wireless ADB bridge (`tools/adb_bridge.py`)
- [x] Add v2 feature test suite (`test_v2_features.py`)
