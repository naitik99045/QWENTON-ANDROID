package ai.qwenton.apex.service

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShizukuExecResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val isSuccess: Boolean
)

object ShizukuManager {

    private const val SHIZUKU_PERMISSION_REQUEST_CODE = 8765

    /**
     * Check if Shizuku service is running on the Android device.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Check if this application has been granted permission to use Shizuku.
     */
    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Request permission from the user through the Shizuku manager dialog.
     */
    fun requestPermission() {
        if (isShizukuAvailable() && !hasPermission()) {
            try {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            } catch (e: Throwable) {
                // Handle permission request failure gracefully
            }
        }
    }

    /**
     * Execute a privileged shell command using Shizuku's UID 2000 (shell) process binder.
     */
    fun executePrivilegedCommand(command: Array<String>): ShizukuExecResult {
        if (!isShizukuAvailable()) {
            return ShizukuExecResult(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku service is not active on this device. Start Shizuku or use Termux local ADB.",
                isSuccess = false
            )
        }

        if (!hasPermission()) {
            return ShizukuExecResult(
                exitCode = -2,
                stdout = "",
                stderr = "Shizuku permission not granted. Authorize Qwenton in the Shizuku app.",
                isSuccess = false
            )
        }

        return try {
            val process = Shizuku.newProcess(command, null, null)
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()

            ShizukuExecResult(
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr,
                isSuccess = (exitCode == 0)
            )
        } catch (e: Throwable) {
            ShizukuExecResult(
                exitCode = -3,
                stdout = "",
                stderr = "Failed to execute via Shizuku: ${e.localizedMessage}",
                isSuccess = false
            )
        }
    }
}
