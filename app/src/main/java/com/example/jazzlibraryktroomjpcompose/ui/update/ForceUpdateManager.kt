package com.example.jazzlibraryktroomjpcompose.ui.update

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

private const val TAG = "ForceUpdateManager"

class ForceUpdateManager(private val context: Context) {

    private var downloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false

    init {
        Log.d(TAG, "ForceUpdateManager initialized with context: ${context.packageName}")
    }

    private fun createDownloadReceiver(): BroadcastReceiver {
        Log.d(TAG, "createDownloadReceiver: creating new BroadcastReceiver instance")
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "DownloadReceiver onReceive: intent=$intent")
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                Log.d(TAG, "DownloadReceiver onReceive: received downloadId=$id, expected=$downloadId")

                if (id != downloadId) {
                    Log.d(TAG, "DownloadReceiver onReceive: ID mismatch, ignoring")
                    return
                }

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                Log.d(TAG, "DownloadReceiver: got DownloadManager service")

                val query = DownloadManager.Query().setFilterById(downloadId)
                Log.d(TAG, "DownloadReceiver: query created for downloadId=$downloadId")

                val cursor = downloadManager.query(query)
                Log.d(TAG, "DownloadReceiver: cursor obtained, count=${cursor.count}")

                if (cursor.moveToFirst()) {
                    Log.d(TAG, "DownloadReceiver: cursor moved to first row")

                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    Log.d(TAG, "DownloadReceiver: statusIndex=$statusIndex, reasonIndex=$reasonIndex")

                    if (statusIndex >= 0) {
                        val status = cursor.getInt(statusIndex)
                        Log.d(TAG, "DownloadReceiver: status value=$status")
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Log.d(TAG, "✅ Download completed successfully")
                                installDownloadedApk()
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else -1
                                Log.e(TAG, "❌ Download failed. Reason code: $reason")
                                Log.d(TAG, "DownloadReceiver: download failed, not proceeding to installation")
                            }
                            else -> {
                                Log.d(TAG, "Download status: $status")
                            }
                        }
                    } else {
                        Log.e(TAG, "❌ COLUMN_STATUS not found in download query")
                    }
                } else {
                    Log.e(TAG, "❌ Download query returned no results")
                }
                cursor.close()
                Log.d(TAG, "DownloadReceiver: cursor closed")
                unregisterDownloadReceiver()
            }
        }
    }

    fun startForceUpdate(apkUrl: String) {
        Log.d(TAG, "🚀 startForceUpdate: entered with apkUrl='$apkUrl'")
        Log.d(TAG, "startForceUpdate: current SDK version = ${Build.VERSION.SDK_INT}")

        // Check if we can install packages FIRST (before downloading)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "startForceUpdate: Android O+, checking install permission")
            if (!canInstallPackages()) {
                Log.e(TAG, "❌ Cannot install packages – requesting permission")
                requestInstallPermission()
                Log.d(TAG, "startForceUpdate: returning due to missing install permission")
                return
            } else {
                Log.d(TAG, "startForceUpdate: install permission granted")
            }
        } else {
            Log.d(TAG, "startForceUpdate: below Android O, no install permission check needed")
        }

        // Check WRITE_EXTERNAL_STORAGE permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.d(TAG, "startForceUpdate: below Android R, checking WRITE_EXTERNAL_STORAGE")
            if (!hasWriteExternalStoragePermission()) {
                Log.e(TAG, "❌ Missing WRITE_EXTERNAL_STORAGE permission")
                Log.d(TAG, "startForceUpdate: returning due to missing storage permission")
                return
            } else {
                Log.d(TAG, "startForceUpdate: WRITE_EXTERNAL_STORAGE permission granted")
            }
        } else {
            Log.d(TAG, "startForceUpdate: Android R+, no WRITE_EXTERNAL_STORAGE check needed")
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        Log.d(TAG, "startForceUpdate: got DownloadManager service")

        // Use public Downloads directory instead of app-specific one
        Log.d(TAG, "startForceUpdate: building DownloadManager.Request")
        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            Log.d(TAG, "startForceUpdate: setting title='Updating Jazz Library'")
            setTitle("Updating Jazz Library")
            Log.d(TAG, "startForceUpdate: setting description='Downloading the latest version...'")
            setDescription("Downloading the latest version...")
            Log.d(TAG, "startForceUpdate: setting notification visibility")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // Download to public Downloads folder
            val destinationDir = Environment.DIRECTORY_DOWNLOADS
            val fileName = "jazz-library-update.apk"
            Log.d(TAG, "startForceUpdate: setting destination to $destinationDir/$fileName")
            setDestinationInExternalPublicDir(destinationDir, fileName)
            Log.d(TAG, "startForceUpdate: setting allowed network types (WIFI+MOBILE)")
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            Log.d(TAG, "startForceUpdate: setAllowedOverRoaming(false)")
            setAllowedOverRoaming(false)
        }

        downloadId = downloadManager.enqueue(request)
        Log.d(TAG, "📥 Download enqueued with ID $downloadId")

        // Register receiver
        Log.d(TAG, "startForceUpdate: creating download receiver")
        downloadReceiver = createDownloadReceiver()
        try {
            Log.d(TAG, "startForceUpdate: registering receiver with ACTION_DOWNLOAD_COMPLETE")
            context.registerReceiver(
                downloadReceiver!!,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
            isReceiverRegistered = true
            Log.d(TAG, "✅ Download receiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to register receiver", e)
            Log.e(TAG, "startForceUpdate: exception message = ${e.message}", e)
        }
        Log.d(TAG, "startForceUpdate: finished")
    }

    private fun unregisterDownloadReceiver() {
        Log.d(TAG, "unregisterDownloadReceiver: entered, isReceiverRegistered=$isReceiverRegistered, receiver=${downloadReceiver != null}")
        if (isReceiverRegistered && downloadReceiver != null) {
            try {
                Log.d(TAG, "unregisterDownloadReceiver: unregistering receiver")
                context.unregisterReceiver(downloadReceiver)
                isReceiverRegistered = false
                Log.d(TAG, "✅ Download receiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
                Log.e(TAG, "unregisterDownloadReceiver: exception = ${e.message}", e)
            }
        } else {
            Log.d(TAG, "unregisterDownloadReceiver: no active receiver to unregister")
        }
        Log.d(TAG, "unregisterDownloadReceiver: exiting")
    }

    private fun installDownloadedApk() {
        Log.d(TAG, "installDownloadedApk: entered")
        // APK is now in public Downloads directory
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "jazz-library-update.apk")
        Log.d(TAG, "installDownloadedApk: expected APK path = ${file.absolutePath}")

        if (!file.exists()) {
            Log.e(TAG, "❌ APK file not found after successful download: ${file.absolutePath}")
            Log.d(TAG, "installDownloadedApk: file does not exist, aborting installation")
            return
        }
        Log.d(TAG, "installDownloadedApk: APK file exists")

        Log.d(TAG, "📦 APK file found: ${file.absolutePath} (${file.length()} bytes)")

        if (!isValidApk(file)) {
            Log.e(TAG, "❌ APK file validation failed - corrupted or invalid file")
            Log.d(TAG, "installDownloadedApk: validation failed, aborting installation")
            return
        }
        Log.d(TAG, "installDownloadedApk: APK validation passed")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "🔧 Using PackageInstaller API (Android 5.0+), SDK=${Build.VERSION.SDK_INT}")
                installApkUsingPackageInstaller(file)
            } else {
                Log.d(TAG, "🔧 Using legacy installation method, SDK=${Build.VERSION.SDK_INT}")
                installApkLegacy(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Installation failed", e)
            Log.e(TAG, "installDownloadedApk: exception = ${e.message}", e)
        }
        Log.d(TAG, "installDownloadedApk: finished")
    }

    private fun isValidApk(file: File): Boolean {
        Log.d(TAG, "isValidApk: entered for file ${file.name}")
        return try {
            val buffer = ByteArray(4)
            FileInputStream(file).use { fis ->
                val bytesRead = fis.read(buffer)
                Log.d(TAG, "isValidApk: read $bytesRead bytes for validation")
            }
            // APK files are ZIP archives, should start with: 50 4B 03 04
            val isValid = buffer[0] == 0x50.toByte() && buffer[1] == 0x4B.toByte()
            Log.d(TAG, "APK validation: ${if (isValid) "✅ valid" else "❌ invalid"} - bytes: ${buffer[0].toUByte()} ${buffer[1].toUByte()} ${buffer[2].toUByte()} ${buffer[3].toUByte()}")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error validating APK", e)
            Log.e(TAG, "isValidApk: exception = ${e.message}", e)
            false
        }
    }

    private fun installApkUsingPackageInstaller(apkFile: File) {
        Log.d(TAG, "installApkUsingPackageInstaller: entered for ${apkFile.absolutePath}")
        val packageInstaller = context.packageManager.packageInstaller
        Log.d(TAG, "installApkUsingPackageInstaller: got PackageInstaller")

        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            Log.d(TAG, "installApkUsingPackageInstaller: setting app package name = ${context.packageName}")
            setAppPackageName(context.packageName)
        }
        Log.d(TAG, "installApkUsingPackageInstaller: SessionParams created")

        val sessionId = try {
            Log.d(TAG, "installApkUsingPackageInstaller: calling createSession")
            packageInstaller.createSession(params)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create PackageInstaller session", e)
            Log.e(TAG, "installApkUsingPackageInstaller: createSession exception = ${e.message}", e)
            return
        }
        Log.d(TAG, "installApkUsingPackageInstaller: session created with ID $sessionId")

        val session = packageInstaller.openSession(sessionId)
        Log.d(TAG, "installApkUsingPackageInstaller: opened session")

        try {
            Log.d(TAG, "installApkUsingPackageInstaller: opening write stream 'base.apk'")
            session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                apkFile.inputStream().use { input ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "installApkUsingPackageInstaller: copied $bytesCopied bytes to session")
                }
            }

            val installationIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(TAG, "installApkUsingPackageInstaller: using Android S+ flags (FLAG_UPDATE_CURRENT|FLAG_IMMUTABLE)")
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                Log.d(TAG, "installApkUsingPackageInstaller: using legacy flags (FLAG_UPDATE_CURRENT)")
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val intent = Intent(context, InstallationReceiver::class.java)
            Log.d(TAG, "installApkUsingPackageInstaller: created intent for InstallationReceiver")

            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, installationIntentFlags)
            Log.d(TAG, "installApkUsingPackageInstaller: created PendingIntent with requestCode=$sessionId")

            Log.d(TAG, "✅ Committing PackageInstaller session $sessionId")
            session.commit(pendingIntent.intentSender)
            Log.d(TAG, "installApkUsingPackageInstaller: commit called, installation in progress")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during session write", e)
            Log.e(TAG, "installApkUsingPackageInstaller: exception = ${e.message}", e)
            session.abandon()
            Log.d(TAG, "installApkUsingPackageInstaller: session abandoned")
        }
        Log.d(TAG, "installApkUsingPackageInstaller: finished")
    }

    private fun installApkLegacy(file: File) {
        Log.d(TAG, "installApkLegacy: entered for ${file.absolutePath}")

        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "installApkLegacy: Android N+, using FileProvider")
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                Log.d(TAG, "installApkLegacy: FileProvider URI = $uri")
                uri
            } catch (e: Exception) {
                Log.e(TAG, "❌ FileProvider error, using file:// URI", e)
                Log.e(TAG, "installApkLegacy: exception = ${e.message}", e)
                Uri.fromFile(file)
            }
        } else {
            Log.d(TAG, "installApkLegacy: below Android N, using file:// URI")
            Uri.fromFile(file)
        }

        Log.d(TAG, "📲 Using ACTION_VIEW to open APK installer: $apkUri")

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.d(TAG, "installApkLegacy: added FLAG_ACTIVITY_NEW_TASK")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Log.d(TAG, "installApkLegacy: added FLAG_GRANT_READ_URI_PERMISSION")
            }
        }

        try {
            Log.d(TAG, "installApkLegacy: starting activity with intent")
            context.startActivity(installIntent)
            Log.d(TAG, "✅ Installation intent launched")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch installation intent", e)
            Log.e(TAG, "installApkLegacy: exception = ${e.message}", e)
        }
        Log.d(TAG, "installApkLegacy: finished")
    }

    fun canInstallPackages(): Boolean {
        Log.d(TAG, "canInstallPackages: entered, SDK=${Build.VERSION.SDK_INT}")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = context.packageManager.canRequestPackageInstalls()
            Log.d(TAG, "Can install packages: $canInstall")
            canInstall
        } else {
            Log.d(TAG, "canInstallPackages: below Android O, returning true (no restriction)")
            true
        }
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        Log.d(TAG, "hasWriteExternalStoragePermission: entered")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val granted = context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "hasWriteExternalStoragePermission: granted = $granted")
            granted
        } else {
            Log.d(TAG, "hasWriteExternalStoragePermission: below Marshmallow, returning true")
            true
        }
    }

    private fun requestInstallPermission() {
        Log.d(TAG, "requestInstallPermission: entered, SDK=${Build.VERSION.SDK_INT}")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.d(TAG, "requestInstallPermission: intent data = ${data}")
        }
        try {
            context.startActivity(intent)
            Log.d(TAG, "✅ Opened install unknown apps settings - user must grant permission")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open settings", e)
            Log.e(TAG, "requestInstallPermission: exception = ${e.message}", e)
        }
    }

    fun cleanup() {
        Log.d(TAG, "cleanup: entered")
        unregisterDownloadReceiver()
        Log.d(TAG, "cleanup: finished")
    }
}