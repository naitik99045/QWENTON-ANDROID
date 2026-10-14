package ai.qwenton.apex.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.regex.Pattern

data class PairingInfo(
    val code: String,
    val port: String,
    val timestamp: Long
)

class QwentonNotificationListener : NotificationListenerService() {

    companion object {
        var lastPairingInfo: PairingInfo? = null
            private set

        private val PAIRING_CODE_PATTERN = Pattern.compile("\\b(\\d{6})\\b")
        private val PORT_PATTERN = Pattern.compile("(?::|port\\s*)(\\d{4,5})\\b", Pattern.CASE_INSENSITIVE)

        var onPairingCodeDetected: ((code: String, port: String) -> Unit)? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: ""
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val combined = "$title $text"

        // Strictly filter only Wireless Debugging notifications
        val isWirelessDebug = pkg == "com.android.settings" || 
                              combined.contains("wireless debugging", ignoreCase = true) ||
                              combined.contains("pairing code", ignoreCase = true)

        if (!isWirelessDebug) {
            // Discard all other user notifications for strict privacy
            return
        }

        val codeMatcher = PAIRING_CODE_PATTERN.matcher(combined)
        val portMatcher = PORT_PATTERN.matcher(combined)

        if (codeMatcher.find()) {
            val code = codeMatcher.group(1) ?: return
            val port = if (portMatcher.find()) portMatcher.group(1) ?: "5555" else "5555"

            lastPairingInfo = PairingInfo(code = code, port = port, timestamp = System.currentTimeMillis())
            onPairingCodeDetected?.invoke(code, port)
        }
    }
}
