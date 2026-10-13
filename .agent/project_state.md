# Project State & Context
**Last Updated:** 2026-10-13 14:34:00 IST

---

## 1. Project Overview

**Qwenton Apex Android** (`ai.qwenton.apex`) is a production-ready, privacy-first mobile AI assistant architecture designed for Android devices, combining:
1. A native Android client built with Jetpack Compose & Material 3 Dark theme, Room SQLite offline history, push-to-talk voice, and OkHttp WebSocket streaming over loopback (`127.0.0.1:8765`).
2. A local Python 3 / FastAPI server hosted inside Termux on the Android device listening strictly on loopback `127.0.0.1:8765`.
3. A deterministic security and tool policy engine enforcing cryptographic HMAC-SHA256 integrity verification, explicit user approval modals, secret redaction, and zero raw shell execution (`shell=True` prohibited).
4. Full support for 100% air-gapped, offline on-device inference using GGUF quantized models with `llama.cpp` in Termux.

---

## 2. Completed Architecture & Milestones

- **Dual-Subsystem Architecture**:
  - Android application operates without third-party API keys or unverified elevated permissions.
  - Termux Python server encapsulates environment execution and AI provider keys.
- **Deterministic Tool & Policy Engine**:
  - Registered tools: `get_time`, `get_battery`, `list_home`, `read_allowed_file`, `install_python_package`, `run_known_script`.
  - Blocked by default: `rm`, `sudo`, `kill`, `chmod`, raw shells.
  - HMAC-SHA256 signatures generated server-side upon proposal; recalculated on approval before execution.
  - 120-second expiration window and single-use execution tokens.
  - Subprocess stdout/stderr buffer capped at 512 KB with 30s timeouts.
- **Offline Persistence & Voice**:
  - Room SQLite database (`qwenton_apex.db`) stores conversations, messages, and command executions locally.
  - Android native `SpeechRecognizer` (Push-to-Talk) and `TextToSpeech` (TTS) playback.
- **Build Readiness**:
  - Gradle 8.7 wrapper configuration and adaptive vector launcher icons (`ic_launcher.xml`, `ic_launcher_round.xml`) in place for error-free AAPT2 compilation.
  - 1-click ADB deployment scripts for Windows (`deploy_device.bat`) and POSIX (`deploy_device.sh`).

---

## 3. Project Status & Test Coverage

| Component | Status | Test Coverage |
|---|---|---|
| **Android Client** (`android-client/`) | ✅ Complete & Hardened | `ModelTest.kt`, `ViewModelTest.kt` |
| **Termux Server** (`termux-server/`) | ✅ Complete & Hardened | `test_health.py`, `test_policy.py`, `test_command_runner.py`, `test_api_contract.py` |
| **Security Audit** (`.agent/security_report.md`) | ✅ 9/9 Patched (Pass) | Full OWASP & CWE Penetration Test Remediated |
| **Hardware Deployment Scripts** (`scripts/`) | ✅ Complete & Verified | Windows & POSIX automated ADB push & install |
| **Offline LLM Integration** (`docs/local-llm-setup.md`) | ✅ Complete | GGUF / llama.cpp local inference configuration |

---

## 4. How to Execute & Deploy

### Quick Deployment to Connected Phone:
```cmd
scripts\deploy_device.bat
```
*(Or `./scripts/deploy_device.sh` on Linux/macOS)*

The system is 100% complete and ready for execution.
