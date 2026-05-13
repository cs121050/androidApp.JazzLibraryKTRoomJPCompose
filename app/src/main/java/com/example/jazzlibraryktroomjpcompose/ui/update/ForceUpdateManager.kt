package com.example.jazzlibraryktroomjpcompose.ui.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class ForceUpdateManager(private val context: Context) {

    private var downloadId: Long = -1
    private val tag = "ForceUpdate"

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != downloadId) return

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Log.d(tag, "Download completed successfully")
                        installDownloadedApk()
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                        Log.e(tag, "Download failed. Reason code: $reason")
                    }
                    else -> Log.d(tag, "Download status: $status")
                }
            } else {
                Log.e(tag, "Download query returned no results")
            }
            cursor.close()
            context.unregisterReceiver(this)
        }
    }

    fun startForceUpdate(apkUrl: String) {
        Log.d(tag, "Starting force update from URL: $apkUrl")

        // Check if we can install packages (Android 8+)
        if (!canInstallPackages()) {
            Log.e(tag, "Cannot install packages – user has not granted permission")
            openInstallUnknownAppsSettings()
            return
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Updating Jazz Library")
            setDescription("Downloading the latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "jazz-library-update.apk")
        }

        downloadId = downloadManager.enqueue(request)
        Log.d(tag, "Download enqueued with ID $downloadId")

        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    private fun installDownloadedApk() {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jazz-library-update.apk")
        if (!file.exists()) {
            Log.e(tag, "APK file not found after successful download")
            return
        }

        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
        Log.d(tag, "Installation intent launched")
    }

    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    private fun openInstallUnknownAppsSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(tag, "Opened install unknown apps settings")
    }
}