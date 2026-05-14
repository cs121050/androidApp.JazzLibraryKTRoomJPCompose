package com.example.jazzlibraryktroomjpcompose.ui.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

/**
 * Receives installation completion status from PackageInstaller
 */
class InstallationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                Log.d("InstallationReceiver", "✅ Installation pending user confirmation (session: $sessionId)")
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d("InstallationReceiver", "✅ Installation successful (session: $sessionId)")
            }
            PackageInstaller.STATUS_FAILURE -> {
                val detailedStatus = intent.getIntExtra(PackageInstaller.EXTRA_STATUS_MESSAGE, -1)
                Log.e("InstallationReceiver", "❌ Installation failed (session: $sessionId, detailed: $detailedStatus)")
            }
            else -> {
                Log.d("InstallationReceiver", "Installation status: $status (session: $sessionId)")
            }
        }
    }
}