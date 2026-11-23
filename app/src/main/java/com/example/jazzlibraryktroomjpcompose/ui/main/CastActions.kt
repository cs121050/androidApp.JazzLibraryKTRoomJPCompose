package com.example.jazzlibraryktroomjpcompose.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

fun openSystemCastSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_CAST_SETTINGS))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Cast settings are unavailable on this device",
            Toast.LENGTH_SHORT
        ).show()
    }
}