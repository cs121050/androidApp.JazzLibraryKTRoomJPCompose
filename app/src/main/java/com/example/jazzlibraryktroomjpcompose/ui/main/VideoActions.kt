package com.example.jazzlibraryktroomjpcompose.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ShareCompat

fun shareYouTubeVideo(context: Context, videoId: String?) {
    if (videoId.isNullOrBlank()) {
        Toast.makeText(context, "No video is playing", Toast.LENGTH_SHORT).show()
        return
    }

    val url = "https://www.youtube.com/watch?v=$videoId"

    val intent = ShareCompat.IntentBuilder(context)
        .setType("text/plain")
        .setText(url)
        .setChooserTitle("Share video")
        .createChooserIntent()

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
    }
}

fun openYouTubeInBrowser(context: Context, videoId: String?) {
    if (videoId.isNullOrBlank()) {
        Toast.makeText(context, "No video is playing", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/watch?v=$videoId")
    ).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)

        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to open the link", Toast.LENGTH_SHORT).show()
    }
}