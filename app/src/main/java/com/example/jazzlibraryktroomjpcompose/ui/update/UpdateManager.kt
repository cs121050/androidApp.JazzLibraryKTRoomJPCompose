package com.example.jazzlibraryktroomjpcompose.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.jazzlibraryktroomjpcompose.domain.models.UpdateInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.coroutines.tasks.await

class UpdateManager(private val context: Context) {

    private val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
        // Set default values (used when no network or first launch)
        setDefaultsAsync(
            mapOf(
                "latest_version" to "1.0",
                "force_min_version" to "1.0",
                "download_url" to "https://your-server.com/app.apk",
                "changelog" to "New update available",
                "last_update_timestamp" to System.currentTimeMillis()
            )
        )
        // Optional: if you want custom fetch interval (e.g., 1 hour), uncomment the lines below
        // But this requires a coroutine scope (suspend function). For simplicity, we skip it here.
        /*
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        setConfigSettingsAsync(settings) // suspend, call elsewhere
        */
    }

    suspend fun fetchUpdateInfo(): UpdateInfo {
        remoteConfig.fetchAndActivate().await()
        return UpdateInfo(
            latestVersion = remoteConfig["latest_version"].asString(),
            forceMinVersion = remoteConfig["force_min_version"].asString(),
            downloadUrl = remoteConfig["download_url"].asString(),
            changeLog = remoteConfig["changelog"].asString(),
            lastUpdateTimestamp = remoteConfig["last_update_timestamp"].asLong()
        )
    }

    fun openDownloadUrl() {
        val url = remoteConfig["download_url"].asString()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getCurrentVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = if (i < parts1.size) parts1[i] else 0
            val p2 = if (i < parts2.size) parts2[i] else 0
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}