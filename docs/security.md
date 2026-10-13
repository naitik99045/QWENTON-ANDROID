# Security & Policy Specifications

## Principles of Defense

### 1. Prohibition of `shell=True`
Under no circumstances does Qwenton Apex pass concatenated AI text or shell strings to `/bin/sh` or `cmd.exe`.
- All subprocesses are invoked strictly as argument vectors:
  `asyncio.create_subprocess_exec(program, *arguments)`

### 2. Cryptographic Command Integrity Hashing
When an AI proposes a command:
1. The server serializes the canonical command tuple:
   `(command_id, program, arguments, working_directory, expires_at)`
2. The server computes an HMAC-SHA256 signature using a runtime secret key.
3. The Android client displays the exact tuple in `CommandApprovalDialog`.
4. When approved, the client sends only `command_id` and the approval boolean.
5. The server recalculates and asserts HMAC equality before spawning the process. Any client-side parameter alteration results in an immediate rejection.

### 3. Path Traversal & Allowlist Enforcement
- File reads are restricted to `$HOME` (`/data/data/com.termux/files/home`).
- Any occurrence of `..` in arguments or paths causes an immediate `PolicyViolationError`.
- Symbolic links resolving outside allowed directories are forbidden.

### 4. Secret Scrubbing & Data Redaction
The audit logging system (`audit_log.py`) and WebSocket broadcasters run regex scrubs on all text:
- Google AI Studio keys: `AIza[0-9A-Za-z-_]{35}`
- OpenAI keys: `sk-[a-zA-Z0-9]{32,}`
- Bearer Authorization headers: `Bearer ...`
Scrubbed values are replaced with `[REDACTED_SECRET]` before persistence or transmission.
