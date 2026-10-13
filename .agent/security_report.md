# Qwenton Security & Debug Audit Report
**Scan Timestamp:** 2026-10-13 16:56:00 IST  
**Overall Posture:** Pass (All 9 Identified Vulnerabilities Remediated & Verified)

---

## Findings Matrix

| ID | Severity | Component / File | Vulnerability / Bug Type | Status |
|---|---|---|---|---|
| **SEC-01** | Medium | `android-client/app/src/main/AndroidManifest.xml:15` | Insecure Backup Data Extraction (OWASP M2) | **[FIXED]** |
| **SEC-02** | High | `android-client/app/src/main/AndroidManifest.xml:21` | Global Cleartext HTTP Traffic Allowed (OWASP M3) | **[FIXED]** |
| **SEC-03** | High | `termux-server/app/tools/files.py:32` | TOCTOU Symlink Arbitrary File Read (CWE-59) | **[FIXED]** |
| **SEC-04** | High | `termux-server/app/command_policy.py:72` | Ambiguous Argument Serialization in HMAC Hash (CWE-138) | **[FIXED]** |
| **SEC-05** | High | `termux-server/app/command_policy.py:79` | Symlink Sandbox Bypass via Lexical `abspath` (CWE-59) | **[FIXED]** |
| **SEC-06** | High | `termux-server/app/routes_commands.py:21` | Replay & Concurrent Approval Double-Execution (CWE-362) | **[FIXED]** |
| **SEC-07** | Medium | `termux-server/app/routes_chat.py:18` | Uncontrolled Memory Allocation / Payload DoS (CWE-400) | **[FIXED]** |
| **SEC-08** | High | `termux-server/app/main.py:46` | Permissive Wildcard CORS / Web Browser CSRF (CWE-942) | **[FIXED]** |
| **SEC-09** | Medium | `android-client/.../NetworkModule.kt:29` | Unchecked Base URL IllegalArgumentException Crash (CWE-20) | **[FIXED]** |

---

## Detailed Remediation Log

### [SEC-01] Insecure Backup Data Extraction
- **Location:** `android-client/app/src/main/AndroidManifest.xml:15`
- **Description:** `android:allowBackup="true"` enabled ADB extraction of private Room databases (`qwenton_apex.db`) containing conversation logs and command executions.
- **Remediation Implemented:** Explicitly configured `android:allowBackup="false"` in the application tag.
- **Verification:** Verified in `AndroidManifest.xml`.

### [SEC-02] Global Cleartext HTTP Traffic Allowed
- **Location:** `android-client/app/src/main/AndroidManifest.xml:21`
- **Description:** Setting global `android:usesCleartextTraffic="true"` allowed the app to emit unencrypted HTTP requests over public Wi-Fi to any external server.
- **Remediation Implemented:** Created `network_security_config.xml` strictly scoping cleartext HTTP exclusively to `127.0.0.1` and `localhost` (Termux local server), while enforcing system TLS/HTTPS for all external requests.
- **Verification:** Verified `network_security_config.xml` reference in `AndroidManifest.xml`.

### [SEC-03] TOCTOU Symlink Arbitrary File Read
- **Location:** `termux-server/app/tools/files.py:32`
- **Description:** `is_path_allowed(file_path)` checked path validity before `Path(file_path).resolve()` dereferenced symlinks. A symlink inside `$HOME` pointing to sensitive system files could bypass sandbox checks.
- **Remediation Implemented:** Reordered operations so `file_path` is canonicalized via `.resolve()` first, and then validated with `is_path_allowed(str(resolved))`.
- **Verification:** Unit tested in test suite.

### [SEC-04] Ambiguous Argument Serialization in HMAC Integrity Hash
- **Location:** `termux-server/app/command_policy.py:72`
- **Description:** Arguments were serialized using naive `','.join(arguments)`. Arguments containing commas (e.g. `["a,b", "c"]` vs `["a", "b,c"]`) generated identical strings, creating signature collisions.
- **Remediation Implemented:** Switched to deterministic `json.dumps(arguments, separators=(',', ':'))` canonicalization.
- **Verification:** Added `test_argument_canonicalization_with_commas` in `test_policy.py`.

### [SEC-05] Symlink Sandbox Bypass via Lexical `abspath`
- **Location:** `termux-server/app/command_policy.py:79`
- **Description:** `is_path_allowed` used `os.path.abspath`, which does not resolve symlink targets, allowing directory traversal if a symlink pointed outside `$HOME`.
- **Remediation Implemented:** Upgraded normalization to `os.path.realpath`, dereferencing symlinks to physical disk paths before checking allowed directory bounds.
- **Verification:** Tested in `test_policy.py`.

### [SEC-06] Replay & Concurrent Approval Double-Execution
- **Location:** `termux-server/app/routes_commands.py:21`
- **Description:** `approve_command` did not enforce that the proposal was in `PENDING` state before locking and spawning subprocesses. Rapid concurrent requests or replays could double-execute commands.
- **Remediation Implemented:** Added state verification asserting `record.status == ApprovalStatus.PENDING`, returning HTTP 409 Conflict if already processed.
- **Verification:** Added `test_command_approval_replay_rejected` in `test_api_contract.py`.

### [SEC-07] Uncontrolled Memory Allocation / Payload DoS
- **Location:** `termux-server/app/routes_chat.py:18`
- **Description:** Missing upper bounds on `request.message`, `request_id`, and `conversation_id` created potential memory exhaustion Denial of Service vulnerability.
- **Remediation Implemented:** Enforced 32 KB character boundary on `message` (returning HTTP 413) and 128-character limit on IDs.
- **Verification:** Added `test_chat_oversized_payload_rejection` in `test_api_contract.py`.

### [SEC-08] Permissive Wildcard CORS / Web Browser CSRF
- **Location:** `termux-server/app/main.py:46`
- **Description:** `allow_origins=["*"]` allowed malicious websites visited in a browser on the Android phone to send cross-origin requests to `http://127.0.0.1:8765`.
- **Remediation Implemented:** Restricted CORS and WebSocket connection headers strictly to local loopback origins (`127.0.0.1` and `localhost`), rejecting untrusted web origins with HTTP 403 or WebSocket code 1008.
- **Verification:** Verified CORS origin configuration in `main.py`.

### [SEC-09] Unchecked Base URL Input Crash
- **Location:** `android-client/.../NetworkModule.kt:29`
- **Description:** Malformed user URLs configured in Settings triggered an unhandled `IllegalArgumentException` in Retrofit, crashing the Android app.
- **Remediation Implemented:** Added `isValidUrl` method using `okhttp3.HttpUrl.parse` with automatic fallback to `http://127.0.0.1:8765/`.
- **Verification:** Added `testNetworkModuleUrlValidation` in `ViewModelTest.kt`.

---

## Next Turn Action Plan
Zero `[PENDING]` issues remain. All 9 discovered security items have been remediated, verified, and integrated into the living codebase.
