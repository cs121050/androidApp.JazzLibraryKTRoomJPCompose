package com.example.jazzlibraryktroomjpcompose.ui.update

import android.app.DownloadManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

class ForceUpdateService : Service() {

    private var downloadId: Long = -1
    private var apkUrl = ""
    private val tag = "ForceUpdateService"
    private lateinit var downloadManager: DownloadManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var monitorRunnable: Runnable

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate: service created")
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "force_update_channel",
                "Force Update",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Background app update"
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(tag, "✅ Notification channel created")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand: called")

        apkUrl = intent?.getStringExtra("APK_URL") ?: ""
        if (apkUrl.isEmpty()) {
            Log.e(tag, "❌ APK_URL missing, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        // Build persistent notification
        val notification = NotificationCompat.Builder(this, "force_update_channel")
            .setContentTitle("Jazz Library Update")
            .setContentText("Downloading latest version...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Start foreground
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(tag, "✅ Started as foreground service")
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Log.e(tag, "❌ Foreground service not allowed", e)
            stopSelf()
            return START_NOT_STICKY
        }

        startDownload(apkUrl)
        return START_STICKY
    }

    private fun startDownload(apkUrl: String) {
        Log.d(tag, "📥 startDownload: $apkUrl")

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Jazz Library Update")
            setDescription("Downloading latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "jazz-library-update.apk")
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setAllowedOverRoaming(false)
        }

        downloadId = downloadManager.enqueue(request)
        Log.d(tag, "✅ Download enqueued with ID: $downloadId")

        // Start polling for download completion
        startMonitoringDownload()
    }

    private fun startMonitoringDownload() {
        monitorRunnable = object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                var success = false
                var localUri: Uri? = null

                val cursor: Cursor? = downloadManager.query(query)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val localUriColumn = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        if (statusColumn >= 0 && localUriColumn >= 0) {
                            val status = it.getInt(statusColumn)
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    val uriString = it.getString(localUriColumn)
                                    if (!uriString.isNullOrEmpty()) {
                                        localUri = Uri.parse(uriString)
                                        success = true
                                        Log.d(tag, "✅ Download successful via polling")
                                    } else {
                                        Log.e(tag, "❌ COLUMN_LOCAL_URI is empty")
                                    }
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    Log.e(tag, "❌ Download failed with status: $status")
                                }
                                else -> {
                                    // Still downloading, check again in 1 second
                                    Log.d(tag, "Download in progress, status=$status")
                                    handler.postDelayed(this, 1000)
                                    return
                                }
                            }
                        } else {
                            Log.e(tag, "❌ Required columns not found")
                        }
                    } else {
                        Log.e(tag, "❌ No download found for id $downloadId")
                    }
                }

                if (success) {
                    localUri?.let { uri ->
                        val file = getFileFromUri(uri)
                        if (file != null && file.exists()) {
                            Log.d(tag, "📦 APK file exists at ${file.absolutePath}, size=${file.length()}")
                            if (isValidApk(file)) {
                                installApk(file)
                            } else {
                                Log.e(tag, "❌ APK validation failed")
                            }
                        } else {
                            Log.e(tag, "❌ Could not locate APK file from URI")
                        }
                    } ?: Log.e(tag, "❌ localUri is null despite success")
                } else {
                    Log.e(tag, "❌ Download was not successful or URI missing")
                }

                // Stop service after handling (whether success or failure)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        // Start the first check immediately (but after a small delay to let download start)
        handler.postDelayed(monitorRunnable, 500)
    }

    private fun getFileFromUri(uri: Uri): File? {
        return if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val dataColumn = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (dataColumn >= 0) {
                        val path = it.getString(dataColumn)
                        if (!path.isNullOrEmpty()) {
                            return File(path)
                        }
                    }
                }
            }
            null
        } else if (uri.scheme == "file") {
            File(uri.path ?: return null)
        } else {
            null
        }
    }

    private fun isValidApk(file: File): Boolean {
        return try {
            val buffer = ByteArray(4)
            FileInputStream(file).use { fis ->
                fis.read(buffer)
            }
            buffer[0] == 0x50.toByte() && buffer[1] == 0x4B.toByte()
        } catch (e: Exception) {
            Log.e(tag, "APK validation error", e)
            false
        }
    }

    private fun installApk(apkFile: File) {
        Log.d(tag, "🔧 installApk: starting installation")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val packageInstaller = packageManager.packageInstaller
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                    setAppPackageName(packageName)
                }
                val sessionId = packageInstaller.createSession(params)
                val session = packageInstaller.openSession(sessionId)
                session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                    apkFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                val intent = Intent(this, InstallationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    sessionId,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )
                session.commit(pendingIntent.intentSender)
                Log.d(tag, "✅ PackageInstaller session committed")
            } catch (e: Exception) {
                Log.e(tag, "❌ PackageInstaller failed", e)
                installApkLegacy(apkFile)
            }
        } else {
            installApkLegacy(apkFile)
        }
    }

    private fun installApkLegacy(apkFile: File) {
        Log.d(tag, "🔧 installApkLegacy: using ACTION_VIEW")
        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startActivity(intent)
        Log.d(tag, "✅ Installation activity launched")
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy: cleaning up")
        handler.removeCallbacks(monitorRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 9999
    }
}