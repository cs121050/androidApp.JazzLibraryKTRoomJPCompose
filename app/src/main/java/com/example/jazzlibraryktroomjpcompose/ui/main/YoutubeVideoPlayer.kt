package com.example.jazzlibraryktroomjpcompose.ui.main

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YoutubeVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Create the view with default auto‑initialization
    val youTubePlayerView = remember(videoId) {
        YouTubePlayerView(context)
    }

    // 2. Manage lifecycle observer + release resources
    DisposableEffect(lifecycleOwner, youTubePlayerView) {
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(youTubePlayerView)
        onDispose {
            lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    // 3. Manage the player listener for the current videoId
    DisposableEffect(youTubePlayerView, videoId) {
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0f)   // or loadVideo() for auto‑play
            }
        }
        youTubePlayerView.addYouTubePlayerListener(listener)
        onDispose {
            youTubePlayerView.removeYouTubePlayerListener(listener)
        }
    }

    // 4. Embed the view
    AndroidView(
        factory = { youTubePlayerView },
        modifier = modifier
    )
}