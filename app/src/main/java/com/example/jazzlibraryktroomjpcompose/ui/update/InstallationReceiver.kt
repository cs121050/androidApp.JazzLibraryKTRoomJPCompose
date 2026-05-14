package com.example.jazzlibraryktroomjpcompose.ui.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

class InstallationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("InstallationReceiver", "🔔 onReceive: installation status broadcast received")

        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)

        Log.d("InstallationReceiver", "🔔 Status code: $status | Session ID: $sessionId")

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                Log.d("InstallationReceiver", "✅ Installation pending user action (session: $sessionId)")
                // Get the confirmation intent from the broadcast
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmIntent != null) {
                    Log.d("InstallationReceiver", "✅ Showing user confirmation dialog")
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                } else {
                    Log.e("InstallationReceiver", "❌ No confirmation intent provided")
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d("InstallationReceiver", "✅ Installation successful (session: $sessionId)")
                // You could notify the user or close the app here
            }
            PackageInstaller.STATUS_FAILURE -> {
                val detailedStatus = intent.getIntExtra(PackageInstaller.EXTRA_STATUS_MESSAGE, -1)
                Log.e("InstallationReceiver", "❌ Installation failed (session: $sessionId, detailed: $detailedStatus)")
                // Optionally, show a user-friendly message
            }
            else -> {
                Log.d("InstallationReceiver", "Installation status: $status (session: $sessionId)")
            }
        }
    }
}