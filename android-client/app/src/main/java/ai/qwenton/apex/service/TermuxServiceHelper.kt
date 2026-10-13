package ai.qwenton.apex.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object TermuxServiceHelper {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    // Termux RUN_COMMAND Intent Extras
    private const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"

    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun startApexServer(
        context: Context,
        scriptPath: String = "/data/data/com.termux/files/home/qwenton-apex-server/start.sh"
    ): Result<Unit> {
        if (!isTermuxInstalled(context)) {
            return Result.failure(IllegalStateException("Termux is not installed on this device."))
        }

        return try {
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                component = ComponentName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
                putExtra(EXTRA_RUN_COMMAND_PATH, scriptPath)
                putExtra(EXTRA_RUN_COMMAND_BACKGROUND, true)
                putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, "0") // Runs in background session
            }
            context.startService(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
