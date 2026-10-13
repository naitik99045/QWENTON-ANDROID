# Architecture & Design Specification

## Overview
**Qwenton Apex Android** utilizes a decoupled, local-loopback client-server topology:
1. **Frontend**: Native Android Application written in Kotlin with Jetpack Compose.
2. **Backend**: Asynchronous Python 3 server powered by FastAPI running locally within the Termux sandbox.

## Communication Topology

```text
+-------------------------+                       +--------------------------+
|      Android Client     |                       |       Termux Server      |
|  (ai.qwenton.apex)      |                       |  (127.0.0.1:8765)        |
+-------------------------+                       +--------------------------+
             |                                                  |
             |--- GET /health --------------------------------->|
             |<-- 200 OK (status: ok) --------------------------|
             |                                                  |
             |--- POST /chat {message: "Check battery"} ------->|
             |<-- 200 OK (type: "tool_request", tool: battery) -|
             |                                                  |
             |--- POST /commands/{cmd_id}/approve ------------->|
             |    (User confirms exact args & working dir)      |
             |                                                  |
             |<-- WebSocket: /ws/events ------------------------|
             |    {"type": "command_started"}                   |
             |    {"type": "command_output", "text": "..."}     |
             |    {"type": "command_completed", "exit_code": 0} |
             |<-- 200 OK (status: APPROVED) --------------------|
```

## Key Modules
1. **Deterministic Execution Policy**: Located in `app/command_policy.py`. Validates tool permissions, inspects arguments for path traversal (`..`), checks program allowlists, and signs proposals with HMAC-SHA256.
2. **Asynchronous Command Runner**: Located in `app/command_runner.py`. Executes subprocesses using `asyncio.create_subprocess_exec` with timeouts and buffer capping (512 KB).
3. **Room Database Integration**: Located in `ai.qwenton.apex.data`. Persists conversations, messages, and terminal executions offline with zero cloud audio or secret leakage.
4. **Voice Pipeline**: Push-to-Talk (PTT) audio transcription using native Android `SpeechRecognizer` and response speech with `TextToSpeech`.
