package ai.qwenton.apex.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

data class InstalledApp(
    val appName: String,
    val packageName: String
)

object AppLauncherHelper {

    /**
     * Retrieve all launchable user-facing applications installed on the Android device.
     */
    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        return resolveInfoList.map {
            InstalledApp(
                appName = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName
            )
        }
    }

    /**
     * Resolve a user's natural query (e.g. "spotify", "youtube", "camera") to an installed package.
     */
    fun findPackage(context: Context, query: String): InstalledApp? {
        val q = query.trim().lowercase()
        val apps = getInstalledApps(context)

        // 1. Exact match on app name
        apps.firstOrNull { it.appName.lowercase() == q }?.let { return it }

        // 2. Starts with / contains match on app name
        apps.firstOrNull { it.appName.lowercase().contains(q) }?.let { return it }

        // 3. Package name contains query
        apps.firstOrNull { it.packageName.lowercase().contains(q) }?.let { return it }

        // 4. Common aliases fallback
        val alias = when (q) {
            "spotify" -> "com.spotify.music"
            "youtube" -> "com.google.android.youtube"
            "chrome", "browser" -> "com.android.chrome"
            "settings" -> "com.android.settings"
            "camera" -> "com.android.camera"
            "whatsapp" -> "com.whatsapp"
            "telegram" -> "org.telegram.messenger"
            "termux" -> "com.termux"
            "shizuku" -> "moe.shizuku.privileged.api"
            "maps" -> "com.google.android.apps.maps"
            "gmail", "mail" -> "com.google.android.gm"
            "clock", "alarm" -> "com.google.android.deskclock"
            "files", "file manager" -> "com.google.android.documentsui"
            else -> null
        }

        if (alias != null) {
            val app = apps.firstOrNull { it.packageName.lowercase().contains(alias) }
            if (app != null) return app
            return InstalledApp(appName = query.replaceFirstChar { it.uppercase() }, packageName = alias)
        }

        return null
    }

    /**
     * Launch an application on device using its package name.
     */
    fun launchApp(context: Context, packageName: String): Result<String> {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Result.success("Launched $packageName successfully.")
        } else {
            // Attempt fallback launch via Shizuku shell if direct intent is absent
            val shizukuRes = ShizukuManager.executePrivilegedCommand(
                arrayOf("monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
            )
            if (shizukuRes.isSuccess) {
                Result.success("Launched $packageName via Shizuku rootless shell.")
            } else {
                Result.failure(Exception("Cannot find launcher activity for package: $packageName"))
            }
        }
    }
}
