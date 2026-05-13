package com.example.jazzlibraryktroomjpcompose.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.jazzlibraryktroomjpcompose.domain.models.UpdateInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.coroutines.tasks.await

class UpdateManager(private val context: Context) {

    private val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
        // Set default values (used when no network or first launch)
        setDefaultsAsync(
            mapOf(
                "latest_version" to 1,
                "force_min_version" to 1,
                "download_url" to "https://google.com",
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
        // Force a fresh fetch (ignore cache)
        remoteConfig.fetch().await()
        remoteConfig.activate().await()

        remoteConfig.fetchAndActivate().await()
        return UpdateInfo(
            latestVersionCode = remoteConfig["latest_version"].asLong().toInt(),
            forceMinVersionCode = remoteConfig["force_min_version"].asLong().toInt(),
            downloadUrl = remoteConfig["download_url"].asString(),
            changeLog = remoteConfig["changelog"].asString(),
            lastUpdateTimestamp = remoteConfig["last_update_timestamp"].asLong()
        )
    }

    fun openDownloadUrl() {
        val url = remoteConfig["download_url"].asString()
        Log.d("UpdateManager", "Opening URL: '$url'")
        if (url.isNullOrEmpty()) {
            Log.e("UpdateManager", "Download URL is empty")
            return
        }
        try {
            // Try with a simple intent first (no extra flags)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to open with simple intent", e)
            // Fallback: use a chooser
            try {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                    "Open with..."
                )
                context.startActivity(chooser)
            } catch (e2: Exception) {
                Log.e("UpdateManager", "Chooser also failed", e2)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getCurrentVersionCode(): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (e: Exception) { 1 }
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