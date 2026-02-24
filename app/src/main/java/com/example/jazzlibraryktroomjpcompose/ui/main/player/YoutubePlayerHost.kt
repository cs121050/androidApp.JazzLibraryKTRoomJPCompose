// YoutubePlayerHost.kt
package com.example.jazzlibraryktroomjpcompose.ui.main.player

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YoutubePlayerHost(
    modifier: Modifier = Modifier,
    onPlayerReady: (YouTubePlayer) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a single instance that survives recomposition
    val youTubePlayerView = remember { YouTubePlayerView(context) }

    // Attach lifecycle observer and clean up
    DisposableEffect(lifecycleOwner, youTubePlayerView) {
        lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    // When the view is created, obtain the player and pass it to the callback
    AndroidView(
        factory = { youTubePlayerView },
        modifier = modifier,
        update = { view ->
            // Disable nested scrolling (prevents the WebView from intercepting scrolls)
            view.isNestedScrollingEnabled = false
            // The view is already created; we need to get the player instance.
            // The library provides a method to get the player, but it may not be ready immediately.
            // We'll add a listener that fires when the player is ready.
            view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    onPlayerReady(youTubePlayer)
                }
            })
        }
    )
}