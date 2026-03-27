package com.example.jazzlibraryktroomjpcompose.ui.main.player

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay

@Composable
fun SmartYoutubePlayerHost(
    key: Any,
    videoId: String?,
    isFullscreen: Boolean = false,
    isMiniMode: Boolean = false,
    onPlayerReady: (YouTubePlayer) -> Unit,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val youTubePlayerView = remember(key) { YouTubePlayerView(context) }
    val currentPlayer = remember { mutableStateOf<YouTubePlayer?>(null) }

    fun captureWebView() {
        findWebView(youTubePlayerView) { capturedWebView ->
            capturedWebView?.let {
                it.settings.javaScriptEnabled = true
                it.settings.useWideViewPort = !isMiniMode  // true for normal, false for mini

                it.webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        Log.d("WebViewConsole", "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
                        return true
                    }
                }

                onWebViewReady(it)
                Log.d("SmartPlayer", "WebView captured and configured (useWideViewPort=${it.settings.useWideViewPort})")
            } ?: Log.e("SmartPlayer", "WebView not found in hierarchy")
        }
    }

    DisposableEffect(lifecycleOwner, youTubePlayerView) {
        lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    DisposableEffect(youTubePlayerView) {
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                Log.d("SmartPlayer", "onReady called")
                currentPlayer.value = youTubePlayer
                onPlayerReady(youTubePlayer)

                // Initial capture will be handled by the LaunchedEffect below
                videoId?.let {
                    Log.d("SmartPlayer", "Loading video: $it")
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        }
        youTubePlayerView.addYouTubePlayerListener(listener)
        onDispose { youTubePlayerView.removeYouTubePlayerListener(listener) }
    }

    // Capture WebView when the player becomes ready or when isMiniMode changes
    LaunchedEffect(isMiniMode, currentPlayer.value) {
        if (currentPlayer.value != null) {
            captureWebView()
        }
    }

    LaunchedEffect(videoId, currentPlayer.value) {
        val player = currentPlayer.value
        if (player != null && videoId != null) {
            Log.d("SmartPlayer", "Reloading video due to videoId change: $videoId")
            player.loadVideo(videoId, 0f)
        }
    }

    AndroidView(
        factory = {
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(youTubePlayerView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
        },
        modifier = modifier
    )
}

private fun findWebView(view: View, onFound: (WebView?) -> Unit) {
    if (view is WebView) {
        onFound(view)
        return
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            findWebView(view.getChildAt(i), onFound)
        }
    }
}