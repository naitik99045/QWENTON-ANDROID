# Troubleshooting Guide

| Issue | Root Cause | Resolution |
|---|---|---|
| **Connection Refused (`127.0.0.1:8765`)** | The Termux Python server is stopped or killed by Android OS battery saver. | Open Termux and run `./start.sh` or tap "Restart Termux Server" in Android client Settings. |
| **HTTP 403 Forbidden** | Command or argument violated deterministic security policy (e.g. `rm`, `sudo`, path traversal). | Check `termux-server/audit.jsonl` for policy violation logs. Only allowlisted tools are permitted. |
| **HTTP 410 Gone** | Command proposal expired after 120 seconds. | Re-issue the prompt to generate a fresh approval token. |
| **RUN_COMMAND Permission Error** | Termux `allow-external-apps` property not enabled. | Add `allow-external-apps = true` to `~/.termux/termux.properties` and run `termux-reload-settings`. |
| **Microphone Permission Denied** | Push-to-Talk permission not granted on Android device. | Open Android App Info -> Permissions -> Allow Microphone. |
| **Empty Battery Data** | `termux-api` package or Termux:API app missing. | Run `pkg install termux-api` inside Termux and install the Termux:API APK from F-Droid. |
