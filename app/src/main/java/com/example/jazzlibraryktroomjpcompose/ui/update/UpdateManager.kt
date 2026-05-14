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

private const val TAG = "UpdateManager"

class UpdateManager(private val context: Context) {

    private val remoteConfig: FirebaseRemoteConfig

    init {
        Log.d(TAG, "UpdateManager constructor: initializing with context=$context")
        remoteConfig = FirebaseRemoteConfig.getInstance().apply {
            Log.d(TAG, "UpdateManager: applying default configs")
            setDefaultsAsync(
                mapOf(
                    "latest_version" to 1,
                    "force_min_version" to 1,
                    "download_url" to "https://google.com",
                    "changelog" to "New update available",
                    "last_update_timestamp" to System.currentTimeMillis()
                )
            )
            Log.d(TAG, "UpdateManager: defaults set asynchronously")
        }
        Log.d(TAG, "UpdateManager constructor: remoteConfig initialized")
    }

    suspend fun fetchUpdateInfo(): UpdateInfo {
        Log.d(TAG, "fetchUpdateInfo: entered")
        try {
            Log.d(TAG, "fetchUpdateInfo: calling remoteConfig.fetchAndActivate()")
            val fetchResult = remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "fetchUpdateInfo: fetchAndActivate completed, result=$fetchResult")

            Log.d(TAG, "fetchUpdateInfo: reading 'latest_version' from remote config")
            val latestVersionCode = remoteConfig["latest_version"].asLong().toInt()
            Log.d(TAG, "fetchUpdateInfo: latestVersionCode = $latestVersionCode")

            Log.d(TAG, "fetchUpdateInfo: reading 'force_min_version' from remote config")
            val forceMinVersionCode = remoteConfig["force_min_version"].asLong().toInt()
            Log.d(TAG, "fetchUpdateInfo: forceMinVersionCode = $forceMinVersionCode")

            Log.d(TAG, "fetchUpdateInfo: reading 'download_url' from remote config")
            val downloadUrl = remoteConfig["download_url"].asString()
            Log.d(TAG, "fetchUpdateInfo: downloadUrl = '$downloadUrl'")

            Log.d(TAG, "fetchUpdateInfo: reading 'changelog' from remote config")
            val changeLog = remoteConfig["changelog"].asString()
            Log.d(TAG, "fetchUpdateInfo: changeLog = '$changeLog'")

            Log.d(TAG, "fetchUpdateInfo: reading 'last_update_timestamp' from remote config")
            val lastUpdateTimestamp = remoteConfig["last_update_timestamp"].asLong()
            Log.d(TAG, "fetchUpdateInfo: lastUpdateTimestamp = $lastUpdateTimestamp")

            val updateInfo = UpdateInfo(
                latestVersionCode = latestVersionCode,
                forceMinVersionCode = forceMinVersionCode,
                downloadUrl = downloadUrl,
                changeLog = changeLog,
                lastUpdateTimestamp = lastUpdateTimestamp
            )
            Log.d(TAG, "fetchUpdateInfo: constructed UpdateInfo = $updateInfo")
            Log.d(TAG, "fetchUpdateInfo: returning successfully")
            return updateInfo
        } catch (e: Exception) {
            Log.e(TAG, "fetchUpdateInfo: exception occurred", e)
            Log.e(TAG, "fetchUpdateInfo: exception message = ${e.message}", e)
            throw e
        }
    }

    fun openDownloadUrl() {
        Log.d(TAG, "openDownloadUrl: entered")
        val url = remoteConfig["download_url"].asString()
        Log.d(TAG, "openDownloadUrl: retrieved URL from remote config = '$url'")

        if (url.isNullOrEmpty()) {
            Log.e(TAG, "openDownloadUrl: Download URL is null or empty, aborting")
            return
        }

        Log.d(TAG, "openDownloadUrl: URL is valid, attempting to open with ACTION_VIEW")
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            Log.d(TAG, "openDownloadUrl: created Intent with action=ACTION_VIEW, data=$url")
            context.startActivity(intent)
            Log.d(TAG, "openDownloadUrl: startActivity called successfully")
        } catch (e: Exception) {
            Log.e(TAG, "openDownloadUrl: Failed to open URL", e)
            Log.e(TAG, "openDownloadUrl: exception message = ${e.message}", e)
        }
        Log.d(TAG, "openDownloadUrl: exiting")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getCurrentVersionCode(): Int {
        Log.d(TAG, "getCurrentVersionCode: entered")
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = packageInfo.longVersionCode.toInt()
            Log.d(TAG, "getCurrentVersionCode: retrieved version code = $versionCode for package ${context.packageName}")
            versionCode
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentVersionCode: failed to get package info", e)
            Log.e(TAG, "getCurrentVersionCode: returning default version 1", e)
            1
        }
    }

    fun compareVersions(v1: String, v2: String): Int {
        Log.d(TAG, "compareVersions: entered with v1='$v1', v2='$v2'")
        val parts1 = v1.split(".").map {
            val intVal = it.toIntOrNull() ?: 0
            if (it.toIntOrNull() == null) {
                Log.w(TAG, "compareVersions: invalid part '$it' in version string '$v1', defaulting to 0")
            }
            intVal
        }
        val parts2 = v2.split(".").map {
            val intVal = it.toIntOrNull() ?: 0
            if (it.toIntOrNull() == null) {
                Log.w(TAG, "compareVersions: invalid part '$it' in version string '$v2', defaulting to 0")
            }
            intVal
        }
        Log.d(TAG, "compareVersions: parts1 = $parts1, parts2 = $parts2")

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = if (i < parts1.size) parts1[i] else 0
            val p2 = if (i < parts2.size) parts2[i] else 0
            Log.d(TAG, "compareVersions: comparing index $i: p1=$p1, p2=$p2")
            if (p1 < p2) {
                Log.d(TAG, "compareVersions: result = -1 (v1 < v2)")
                return -1
            }
            if (p1 > p2) {
                Log.d(TAG, "compareVersions: result = 1 (v1 > v2)")
                return 1
            }
        }
        Log.d(TAG, "compareVersions: result = 0 (equal)")
        return 0
    }
}