package ai.qwenton.apex.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ServerProvisioner {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

    /**
     * The universal 1-line Termux on-device setup command (zero PC/laptop required).
     */
    const val ONE_LINE_SETUP_COMMAND =
        "pkg update -y && pkg install -y python git termux-api android-tools && termux-setup-storage && mkdir -p ~/qwenton-apex-server && cp -r /sdcard/Download/qwenton-apex-server/* ~/qwenton-apex-server/ 2>/dev/null || true && cd ~/qwenton-apex-server && python3 -m venv venv && ./venv/bin/pip install fastapi uvicorn httpx pydantic python-dotenv && chmod +x start.sh 2>/dev/null || true && ./venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8765"

    /**
     * Copy text to the Android system clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Qwenton Setup Command") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Export installer script from assets to the public Download folder on the phone.
     */
    fun exportInstallerScript(context: Context): Result<File> {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val targetFile = File(downloadDir, "install_qwenton.sh")
            context.assets.open("install_qwenton.sh").use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.setExecutable(true, false)
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export the bundled termux-server payload from APK assets directly to phone storage.
     */
    fun exportServerPayload(context: Context): Result<File> {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val serverDestDir = File(downloadDir, "qwenton-apex-server")
            if (!serverDestDir.exists()) serverDestDir.mkdirs()

            copyAssetFolder(context, "termux-server", serverDestDir)
            exportInstallerScript(context)
            Result.success(serverDestDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        val assetManager = context.assets
        val list = assetManager.list(assetPath) ?: return
        for (file in list) {
            val subAsset = if (assetPath.isEmpty()) file else "$assetPath/$file"
            val subTarget = File(targetDir, file)
            val subList = assetManager.list(subAsset)
            if (subList != null && subList.isNotEmpty()) {
                subTarget.mkdirs()
                copyAssetFolder(context, subAsset, subTarget)
            } else {
                subTarget.parentFile?.mkdirs()
                try {
                    assetManager.open(subAsset).use { input ->
                        FileOutputStream(subTarget).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    // Ignored or directory entry
                }
            }
        }
    }

    /**
     * Check if Termux is installed on device.
     */
    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if Shizuku is installed on device.
     */
    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open Termux app directly on device.
     */
    fun launchTermuxApp(context: Context): Result<String> {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(TERMUX_PACKAGE)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Result.success("Termux launched.")
        } else {
            Result.failure(Exception("Termux is not installed. Please install Termux from F-Droid."))
        }
    }

    /**
     * Open Shizuku app directly or navigate to Google Play / GitHub to download it.
     */
    fun launchOrInstallShizuku(context: Context) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // Open Shizuku on Play Store / Browser
            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$SHIZUKU_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(playIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    }

    /**
     * Open Android Developer Options so the user can enable Wireless Debugging.
     */
    fun openDeveloperOptions(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Open Android Notification Listener settings so user can enable auto-pairing.
     */
    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
        }
    }
}
