# Master Prompt: Build Qwenton Apex Android with Termux

Copy everything below into your AI coding agent. Use it as the project specification and execution plan.

---

## OFFICIAL PRODUCT IDENTITY

Use these names consistently throughout the project:

- Product name: **Qwenton Apex Android**
- Assistant name: **Qwenton Apex**
- Android application ID and namespace: `ai.qwenton.apex`
- Android source package: `ai.qwenton.apex`
- Termux server display name: **Qwenton Apex Local Server**
- Termux project directory: `~/qwenton-apex-server/`
- Termux script directory: `~/.qwenton/scripts/`
- Root repository directory: `qwenton-apex-android/`
- Main Android files: `QwentonApp.kt`, `QwentonViewModel.kt`, `QwentonRepository.kt`, `QwentonDatabase.kt`, `QwentonApi.kt`

Do not use `Jarvis`, `com.example`, `com.example.jarvis`, or `com.example.qwenton` in user-facing names, package declarations, documentation, file paths, API responses, or logs. The package name `ai.qwenton.apex` must be treated as permanent once the project is published.

Qwenton Apex is a **privacy-first local execution architecture with optional remote or local AI models**. The Android app and Termux tools run locally. OpenAI, Gemini, Anthropic, or other hosted models are optional remote providers. A fully offline mode requires a local model such as a compatible GGUF/llama.cpp setup and must not be described as remote-model privacy.

## ROLE

You are a senior Android, Kotlin, Python, security, DevOps, and UX engineer. Build a complete Qwenton Apex Android assistant application in small, testable stages.

The project must have:

1. A polished native Android application.
2. A Python AI server running inside Termux on the Android device.
3. Local HTTP and WebSocket communication between the Android app and the Termux server.
4. A safe tool and command-execution system.
5. Voice input and text-to-speech support.
6. Conversation history and execution logs.
7. Clear error messages and recovery behavior.
8. A buildable, testable, documented project.

Do not create a fake demo that only looks complete. Implement the project in verified stages. Every stage must compile, run, and be tested before the next stage begins.

---

## IMPORTANT EXECUTION RULES

Follow these rules throughout the project:

1. **Do not skip stages.** Build the smallest working version first.
2. **Do not change multiple major technologies during implementation.** Use the stack defined below.
3. **Do not copy incompatible Gradle versions from random tutorials.** Use versions compatible with the installed Android Studio and Gradle wrapper.
4. **After every meaningful change, run the appropriate tests or build command.**
5. **When an error appears, stop and diagnose it before adding more features.**
6. **Never hide errors.** Display useful messages in the UI and write useful local logs.
7. **Never execute raw AI-generated shell text.** The AI may propose a structured tool action, but a deterministic policy layer must validate it.
8. **Never store API keys in the Android app, Git, screenshots, chat messages, or logs.** Keep model-provider secrets in the Termux server environment.
9. **Never use `shell=True` with AI-generated or user-generated command strings.** Use fixed executable names, arrays of arguments, allowlists, validation, timeouts, and output limits.
10. **Never silently perform destructive or security-sensitive actions.** Require explicit user approval.
11. **Do not use an always-listening microphone in the first version.** Implement push-to-talk first.
12. **Do not claim a feature works until it has been tested on a physical Android device or a clearly documented emulator substitute.**
13. **Do not generate huge files without explaining them.** Keep code modular.
14. **Before editing existing files, inspect them.** Do not overwrite unknown user work.
15. **At the beginning of every stage, state the goal, files to be changed, commands to run, and acceptance criteria.**
16. **At the end of every stage, report changed files, tests run, results, known limitations, and the exact next step.**

If a requirement is ambiguous, choose the safest reasonable default, record the assumption, and continue. Ask for clarification only when the ambiguity would materially change security, data loss, architecture, or user intent.

---

## PRODUCT NAME AND PRODUCT IDEA

Product name: **Qwenton Apex Android**

The user should be able to:

- Chat with an AI assistant.
- Ask the assistant to explain information.
- Ask the assistant to inspect approved device information.
- Ask the assistant to perform approved automation tasks.
- See when the assistant is offline or connected.
- Approve or reject risky operations.
- See command output in a readable terminal panel.
- Speak to the assistant using push-to-talk.
- Hear the assistant's answer using Android text-to-speech.
- Review conversation and command history.
- Stop a running request or command.
- Configure enabled skills and server settings.

The assistant must feel like a modern mobile assistant, not merely a black terminal screen.

---

## REQUIRED TECHNOLOGY STACK

### Android client

Use:

- Kotlin (use the current version selected by the installed Android Studio template)
- Android Studio
- Jetpack Compose
- Material 3
- AndroidX
- ViewModel
- Kotlin Coroutines
- StateFlow
- Navigation Compose
- Room for local history
- Retrofit or Ktor Client for HTTP
- OkHttp WebSocket or Ktor WebSocket client for streaming
- Android SpeechRecognizer for push-to-talk
- Android TextToSpeech for spoken output
- Android foreground service only when a later stage genuinely requires persistent background work
- Gradle and Android Gradle Plugin for building

Do not use Flutter, React Native, Kivy, or .NET MAUI for the Android client in this project.

### Termux server

Use:

- Termux
- Python 3
- Python virtual environment
- FastAPI
- Uvicorn
- Pydantic
- httpx
- WebSockets
- python-dotenv
- pytest
- asyncio

The Python server runs inside Termux and listens only on `127.0.0.1` by default.

### Communication

Use:

- HTTP for health checks, ordinary requests, command proposals, approvals, and history.
- WebSocket for streaming assistant output, progress events, and terminal output.
- JSON with explicit schemas.
- Request IDs and command IDs to prevent duplicate operations.

### Termux integration

Use Termux `RUN_COMMAND` only for controlled, known scripts such as starting or stopping the Qwenton Apex server. Do not pass arbitrary AI-generated shell text through it.

Use Termux:API only for explicitly enabled capabilities such as battery status, notification, vibration, or text-to-speech-related device functions.

---

## REQUIRED REPOSITORY STRUCTURE

Create this structure, adapting package names only if necessary:

```text
qwenton-apex-android/
├── android-client/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradle/
│   └── app/
│       ├── build.gradle.kts
│       └── src/
│           ├── main/
│           │   ├── AndroidManifest.xml
│           │   ├── java/ai/qwenton/apex/
│           │   │   ├── MainActivity.kt
│           │   │   ├── QwentonApp.kt
│           │   │   ├── data/
│           │   │   │   ├── ConversationDao.kt
│           │   │   │   ├── ConversationEntity.kt
│           │   │   │   ├── ExecutionDao.kt
│           │   │   │   ├── ExecutionEntity.kt
│           │   │   │   └── QwentonDatabase.kt
│           │   │   ├── domain/
│           │   │   │   ├── CommandPolicy.kt
│           │   │   │   ├── QwentonRepository.kt
│           │   │   │   └── Models.kt
│           │   │   ├── network/
│           │   │   │   ├── ApiModels.kt
│           │   │   │   ├── QwentonApi.kt
│           │   │   │   ├── NetworkModule.kt
│           │   │   │   └── WebSocketClient.kt
│           │   │   ├── ui/
│           │   │   │   ├── ChatScreen.kt
│           │   │   │   ├── CommandApprovalDialog.kt
│           │   │   │   ├── TerminalOutputScreen.kt
│           │   │   │   ├── SkillsScreen.kt
│           │   │   │   ├── SettingsScreen.kt
│           │   │   │   ├── HistoryScreen.kt
│           │   │   │   ├── Components.kt
│           │   │   │   └── theme/
│           │   │   │       ├── Color.kt
│           │   │   │       ├── Theme.kt
│           │   │   │       └── Type.kt
│           │   │   ├── voice/
│           │   │   │   ├── SpeechRecognizerManager.kt
│           │   │   │   └── TextToSpeechManager.kt
│           │   │   └── service/
│           │   │       └── QwentonConnectionService.kt
│           │   └── res/
│           │       ├── drawable/
│           │       ├── mipmap/
│           │       └── values/
│           └── test/
│
└── termux-server/
    ├── app/
    │   ├── __init__.py
    │   ├── main.py
    │   ├── config.py
    │   ├── models.py
    │   ├── errors.py
    │   ├── routes_health.py
    │   ├── routes_chat.py
    │   ├── routes_commands.py
    │   ├── routes_history.py
    │   ├── ai_client.py
    │   ├── tool_router.py
    │   ├── command_policy.py
    │   ├── command_runner.py
    │   ├── audit_log.py
    │   └── tools/
    │       ├── battery.py
    │       ├── time_info.py
    │       ├── files.py
    │       └── safe_shell.py
    ├── tests/
    │   ├── test_health.py
    │   ├── test_policy.py
    │   ├── test_command_runner.py
    │   └── test_api_contract.py
    ├── requirements.txt
    ├── .env.example
    ├── .gitignore
    ├── start.sh
    └── README.md
```

Create a root `README.md` that explains how the two projects work together.

---

## REQUIRED USER EXPERIENCE

### Visual style

Use a professional dark Material 3 theme by default:

- Dark charcoal background.
- Slightly lighter surface cards.
- One primary blue or violet accent.
- Green for safe status.
- Amber for approval-required status.
- Red for danger or failure.
- Normal Material typography everywhere except terminal output.
- Monospace typography only inside command/output panels.
- Rounded cards and consistent spacing.
- Avoid excessive glowing effects, animations, and decorative robot images.

### Required screens

Implement these screens:

1. **Chat screen**
   - Connection status.
   - Conversation list.
   - User and assistant message bubbles.
   - Streaming response indicator.
   - Text input.
   - Send button.
   - Stop button.
   - Push-to-talk microphone button.

2. **Command approval dialog**
   - Exact action or command.
   - Reason.
   - Risk level.
   - Working directory.
   - Arguments.
   - Files or network access if known.
   - Cancel.
   - Run once.
   - Optional future “always allow this exact tool” control.

3. **Terminal output screen**
   - Running/completed/failed state.
   - Command label.
   - Standard output.
   - Standard error.
   - Exit code.
   - Start time.
   - Duration.
   - Cancel.
   - Copy output.

4. **Skills screen**
   - Enable or disable each tool.
   - Explain the permission required.
   - Show whether the skill is safe, approval-required, or unavailable.

5. **History screen**
   - Conversations.
   - Executions.
   - Search/filter.
   - Delete selected history.
   - Clear all history with confirmation.

6. **Settings screen**
   - Server URL and port.
   - Connection test.
   - Voice settings.
   - Theme.
   - Command policy.
   - Allowed directories.
   - Privacy and data controls.
   - Emergency stop.

### Required UI states

Every network-backed screen must have:

- Loading state.
- Success state.
- Empty state.
- Offline state.
- Permission-denied state.
- Timeout state.
- Server-error state.
- Retry action.

Do not leave blank screens or uncaught exceptions.

---

## REQUIRED API CONTRACT

Implement these endpoints.

### Health

```http
GET /health
```

Response:

```json
{
  "status": "ok",
  "service": "qwenton-apex",
  "version": "0.1.0",
  "server_time": "2026-01-01T00:00:00Z"
}
```

### Chat request

```http
POST /chat
Content-Type: application/json
```

Request:

```json
{
  "request_id": "req_123",
  "conversation_id": "conv_123",
  "message": "Check my battery",
  "client_version": "0.1.0"
}
```

Response for a normal answer:

```json
{
  "type": "assistant_message",
  "request_id": "req_123",
  "text": "I can check the battery status.",
  "requires_approval": false
}
```

Response for a tool proposal:

```json
{
  "type": "tool_request",
  "request_id": "req_123",
  "tool": "get_battery",
  "arguments": {},
  "risk": "safe",
  "requires_approval": false,
  "explanation": "Read battery information from the device."
}
```

Response for a risky command proposal:

```json
{
  "type": "command_request",
  "request_id": "req_123",
  "command_id": "cmd_123",
  "program": "pkg",
  "arguments": ["install", "requests"],
  "working_directory": "/data/data/com.termux/files/home",
  "risk": "medium",
  "requires_approval": true,
  "explanation": "Install the requests Python package."
}
```

### Approve command

```http
POST /commands/{command_id}/approve
```

Request:

```json
{
  "approval_id": "approval_123",
  "approved": true,
  "approved_by": "user"
}
```

The server must verify that the command still matches the command originally shown to the user. Never approve a modified command using an old approval.

### Cancel command

```http
POST /commands/{command_id}/cancel
```

### History

```http
GET /history
DELETE /history/{id}
```

### WebSocket events

```text
/ws/events
```

Events:

```json
{"type":"connected"}
{"type":"assistant_delta","request_id":"req_123","text":"Hello"}
{"type":"assistant_complete","request_id":"req_123"}
{"type":"command_started","command_id":"cmd_123"}
{"type":"command_output","command_id":"cmd_123","stream":"stdout","text":"..."}
{"type":"command_output","command_id":"cmd_123","stream":"stderr","text":"..."}
{"type":"command_completed","command_id":"cmd_123","exit_code":0}
{"type":"error","code":"TIMEOUT","message":"The request timed out."}
```

Use explicit Pydantic models and matching Kotlin data classes. Write API contract tests.

---

## SECURITY AND COMMAND POLICY

This is a mandatory part of the project, not an optional enhancement.

### Never do this

```python
os.system(ai_generated_text)
subprocess.run(ai_generated_text, shell=True)
```

### Use a tool registry

Start with these tools:

```text
get_time
get_battery
list_home
read_allowed_file
install_python_package
run_known_script
```

Classify them:

```text
Safe:
  get_time
  get_battery
  list_home

Approval required:
  read_allowed_file
  install_python_package
  run_known_script

Blocked by default:
  delete files
  change permissions
  kill processes
  modify Android settings
  access private keys
  read arbitrary application data
  install unknown software
  execute arbitrary shell text
```

### Validation requirements

For every tool:

- Validate the tool name.
- Validate every argument type.
- Validate every argument value.
- Restrict file paths to configured directories.
- Reject path traversal such as `../`.
- Use fixed program paths or fixed program names.
- Use argument arrays.
- Set a timeout.
- Limit stdout and stderr size.
- Record exit code.
- Record approval state.
- Redact secrets from logs.
- Generate a unique request ID and command ID.
- Prevent duplicate execution when a request is retried.

### Approval requirements

The Android UI must show the exact structured action before execution. Approval must be:

- Explicit.
- Tied to one command ID.
- Invalidated if arguments change.
- Logged with timestamp.
- Rejected when expired.
- Cancelable.

---

## IMPLEMENTATION STAGES

### Stage 0: Inspect the environment

Before creating files:

1. Inspect the current directory.
2. Check whether an Android project already exists.
3. Check whether a Termux server already exists.
4. Check Android Studio/Gradle/JDK availability if working locally.
5. Check Python and Termux availability if working on the device.
6. Do not overwrite existing code.

Report the environment before proceeding.

Acceptance criteria:

- Existing files are known.
- The intended project directory is confirmed.
- No user data is deleted.

### Stage 1: Create the Android shell

Create the Android project with:

- Empty Compose Activity.
- Material 3 theme.
- Navigation.
- Chat screen placeholder.
- Settings placeholder.
- Skills placeholder.
- History placeholder.

Acceptance criteria:

```bash
./gradlew assembleDebug
./gradlew test
```

Both commands succeed.

### Stage 2: Create the Python server shell

Create:

- Virtual environment instructions.
- FastAPI app.
- `/health` endpoint.
- `/chat` fake response endpoint.
- WebSocket connection endpoint.
- Pytest tests.

Acceptance criteria:

```bash
python -m pytest
uvicorn app.main:app --host 127.0.0.1 --port 8765
curl http://127.0.0.1:8765/health
```

The health response must be valid JSON.

### Stage 3: Connect Android to `/health`

Implement:

- API client.
- Server URL setting.
- Connection test.
- Connected/offline indicator.
- Timeout and retry behavior.

Acceptance criteria:

- The Android app can test the server.
- The UI distinguishes connected, refused, timeout, and malformed responses.
- No UI thread blocking occurs.

### Stage 4: Connect the chat flow

Implement:

- Chat request models.
- ViewModel.
- Repository.
- Fake assistant response.
- Conversation history in memory first.
- Loading, success, error, and cancellation.

Acceptance criteria:

- User sends a message.
- The server receives it.
- The response appears in the UI.
- A failed server returns a readable error.
- Repeated taps do not create duplicate requests.

### Stage 5: Add Room history

Implement:

- Conversation entity.
- Message entity.
- Execution entity.
- DAOs.
- Database migration strategy.
- History screen.

Acceptance criteria:

- Messages survive app restart.
- History can be deleted with confirmation.
- Secrets are not stored in message history.

### Stage 6: Add streaming WebSocket output

Implement:

- WebSocket connection manager.
- Reconnection with capped exponential backoff.
- Streaming assistant message updates.
- Stop/cancel action.
- Duplicate event protection.
- Connection lifecycle handling.

Acceptance criteria:

- Partial assistant output appears progressively.
- Closing the screen does not leak a WebSocket.
- Reconnecting does not duplicate messages.
- Server errors become visible UI states.

### Stage 7: Add tool routing without shell execution

Implement structured tool proposals only.

Start with:

- `get_time`.
- `get_battery`.
- `list_home`.

At this stage, the server may return mock results. It must not execute arbitrary commands.

Acceptance criteria:

- Tool proposals are structured.
- The UI shows a tool card.
- Safe tools can be run.
- Risky tools are not executed.

### Stage 8: Add the command approval system

Implement:

- Command policy.
- Risk labels.
- Approval dialog.
- Approval expiration.
- Command hash or immutable command record.
- Audit logs.
- Cancellation.
- Output-size limits.
- Timeouts.

Acceptance criteria:

- A risky command always requires approval.
- Reject means no process starts.
- Approve runs only the exact displayed command.
- Changing an argument invalidates approval.
- A timeout terminates or safely abandons the process and reports the result.

### Stage 9: Add controlled Termux commands

Implement only allowlisted commands and known scripts.

Test first with:

```text
pwd
ls -la
date
termux-battery-status
```

Do not begin with package installation or file deletion.

Acceptance criteria:

- Output and errors are separate.
- Exit code is displayed.
- The command can be canceled.
- The Android app shows a clear Termux permission error when permission is missing.

### Stage 10: Add Termux RUN_COMMAND integration

Implement:

- Termux package detection.
- User setup instructions.
- `com.termux.permission.RUN_COMMAND` declaration where required.
- Intent construction using documented actions and extras.
- Graceful handling of Termux missing, disabled, denied, or incompatible versions.
- A known `start.sh` script only.

Acceptance criteria:

- The app can request starting the known server script.
- The app does not pass arbitrary chat text as a command.
- The user understands what permission is being granted.

### Stage 11: Add the real AI client

Create an AI client behind an interface so it can be tested with a fake client.

Requirements:

- API key remains on the Termux server.
- Timeouts.
- Retry only safe/idempotent requests.
- Request-size limits.
- Redaction of secrets.
- Structured tool-call output.
- Fallback response if the model is unavailable.
- Conversation context limits.

The AI may choose from the registered tools only. It may not invent executable commands.

Acceptance criteria:

- Normal questions return assistant text.
- Tool requests are validated before execution.
- Invalid tool calls return a safe explanation.
- Model failure does not crash the server.

### Stage 12: Add voice

Implement push-to-talk:

1. Request microphone permission.
2. Start Android SpeechRecognizer.
3. Place recognized text in the composer.
4. Let the user edit before sending.
5. Send through the existing chat pipeline.
6. Speak final assistant text using TextToSpeech when enabled.

Acceptance criteria:

- Permission denial is understandable.
- No microphone remains active after cancellation.
- Voice errors do not crash the app.
- Spoken output can be stopped.

### Stage 13: Add production hardening

Implement:

- Release build configuration.
- Proguard/R8 review.
- Physical-device testing.
- Offline states.
- Battery behavior review.
- Accessibility labels.
- Content descriptions.
- Large-font testing.
- Rotation and process-death testing.
- Network timeout testing.
- Termux process restart testing.
- Privacy documentation.
- User-facing setup wizard.

Acceptance criteria:

- Debug APK builds.
- Release AAB builds.
- Tests pass.
- No secrets are in the repository.
- The app explains every denied permission and unavailable service.

---

## REQUIRED TEST PLAN

### Android tests

Write tests for:

- ViewModel state transitions.
- Empty message validation.
- Duplicate-send prevention.
- Request cancellation.
- Error mapping.
- Command approval state.
- Approval expiration.
- JSON serialization.
- Database operations.
- Navigation.

### Python tests

Write tests for:

- Health endpoint.
- Chat schema validation.
- Unknown tool rejection.
- Invalid arguments.
- Path traversal rejection.
- Blocked program rejection.
- Timeout behavior.
- Output-size limit.
- Duplicate command request.
- Approval mismatch.
- Audit-log redaction.

### Manual tests

Test on a physical device:

- Termux installed.
- Termux missing.
- Termux permission denied.
- Server stopped.
- Server restarted.
- Wi-Fi disabled.
- Device screen locked.
- App backgrounded.
- App process killed.
- Microphone denied.
- Text-to-speech unavailable.
- Large terminal output.
- Command timeout.
- AI provider unavailable.
- Low battery.
- Rotation.
- Dark mode and light mode.
- Large font size.

---

## ERROR-SOLVING PROCEDURE

When any error occurs, produce a diagnostic report with:

```text
Stage:
Action:
Exact error:
Layer:
Last known working state:
Files changed since then:
Commands already run:
Likely causes:
Smallest next diagnostic:
Fix applied:
Validation result:
```

Use this layer map:

| Error | First place to check |
|---|---|
| Kotlin compile error | Imports, types, package names, generated code |
| Compose runtime crash | Logcat and composable state/lifecycle |
| Gradle sync failure | JDK, Gradle wrapper, AGP, Kotlin, repositories |
| Missing dependency | Coordinates, repository, version compatibility |
| App crash on launch | AndroidManifest and Logcat |
| HTTP refused | Uvicorn process, port, URL, localhost |
| HTTP 422 | JSON field names and Pydantic model |
| WebSocket closes | Server log, cancellation, reconnect logic |
| Command not found | Termux PATH and package installation |
| Permission denied | Android permission, Termux permission, working directory |
| Termux Intent failure | Action, extras, package visibility, external-app setting |
| Background process killed | Foreground service and battery restrictions |
| UI freeze | Main-thread work and coroutine usage |
| Empty command output | stdout/stderr capture and exit code |

Never “fix” an error by disabling security, removing validation, hiding exceptions, or executing arbitrary shell text.

---

## DOCUMENTATION REQUIREMENTS

Create and maintain:

1. Root `README.md`.
2. `android-client/README.md`.
3. `termux-server/README.md`.
4. `termux-server/.env.example`.
5. `docs/architecture.md`.
6. `docs/setup-termux.md`.
7. `docs/security.md`.
8. `docs/troubleshooting.md`.
9. `docs/api-contract.md`.
10. `CHANGELOG.md`.

The documentation must include exact commands, expected outputs, screenshots only when useful, and known limitations.

---

## REQUIRED FINAL REPORT

When the project is complete, report:

1. What was implemented.
2. Exact project tree.
3. Android build command.
4. Python test command.
5. Device installation command.
6. Termux setup commands.
7. How to start and stop the server.
8. How to connect the Android app.
9. Which tools are enabled.
10. Which tools are intentionally blocked.
11. Tests passed.
12. Tests not run and why.
13. Known limitations.
14. Security review results.
15. Exact next steps for future versions.

Do not say “complete” if the app has only been compiled but not tested. Clearly distinguish:

```text
Implemented
Compiled
Unit-tested
Manually tested
Tested on physical device
Not yet tested
```

---


## REQUIRED CORRECTIONS BEFORE IMPLEMENTATION

1. **Use current Android tooling.** Do not hard-code old Kotlin, Compose, AGP, Gradle, compile SDK, or target SDK versions from tutorials. Start from the current Android Studio template, confirm the JDK/Gradle/AGP compatibility, compile once, and then lock versions. For Play Store release, verify the current target API requirement at implementation time.

2. **Use server-side command approvals.** The Termux server creates and stores the immutable pending command record. Canonicalize the tool, program, arguments, working directory, risk, and expiry; calculate a SHA-256 integrity hash; return an opaque command ID; show the exact command in Android; and accept only the command ID plus the user approval action. Recalculate and compare the hash server-side before execution. Never trust a command payload reconstructed by the Android client.

3. **Do not claim every mode is on-device.** Local execution and local storage are separate from model inference. Label hosted-model requests as remote. Add a clearly named offline/local-model mode only when a local model has actually been configured and tested.

4. **Verify Termux integration against current documentation.** Confirm the exact RUN_COMMAND action, extras, package visibility rules, `com.termux.permission.RUN_COMMAND`, external-app configuration, compatible installation source, and result-delivery behavior. Do not copy an old Intent example without checking the installed Termux version.

5. **Use the permanent package name.** All Android package declarations, namespace settings, manifest values, tests, and source directories must use `ai.qwenton.apex`.

## FIRST ACTION

Begin now with **Stage 0: Inspect the environment**.

Do not generate the entire project in one uncontrolled response. First inspect the workspace, report what exists, and propose the first small implementation batch. Then execute Stage 1 and continue only after its acceptance criteria pass.

---

## CORE DESIGN PRINCIPLE

Build a reliable assistant, not an unrestricted shell with a chatbot attached.

The AI can reason and propose actions. The deterministic application policy decides what may happen. The user approves risky actions. The Android UI makes every important state visible. Every stage is testable, reversible, and documented.
