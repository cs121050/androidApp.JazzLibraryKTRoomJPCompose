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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
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
    val webView = remember { mutableStateOf<WebView?>(null) }
    var webViewCaptured by remember { mutableStateOf(false) }

    // Track the current size of the player container (in device pixels)
    var currentSize by remember { mutableStateOf(IntSize.Zero) }

    // --- Helper: inject viewport meta with the container's actual width in CSS pixels ---
    fun injectDynamicViewport(webView: WebView, containerWidthPx: Int) {
        val density = context.resources.displayMetrics.density
        val cssWidth = (containerWidthPx / density).toInt()
        val js = """
            (function() {
                // Remove any existing viewport meta tags
                var metas = document.getElementsByTagName('meta');
                for (var i = 0; i < metas.length; i++) {
                    if (metas[i].name === 'viewport') {
                        metas[i].parentNode.removeChild(metas[i]);
                        break;
                    }
                }
                // Create new meta with exact container width
                var meta = document.createElement('meta');
                meta.name = 'viewport';
                meta.content = 'width=$cssWidth, initial-scale=1.0, user-scalable=no';
                document.head.appendChild(meta);
                // Force the player to re‑evaluate its layout
                window.dispatchEvent(new Event('resize'));
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
        Log.d(
            "SmartPlayer",
            "Injected viewport width = $cssWidth (container px = $containerWidthPx, density = $density)"
        )
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

                // Capture WebView only once
                if (!webViewCaptured) {
                    findWebView(youTubePlayerView) { capturedWebView ->
                        capturedWebView?.let {
                            // --- Configure WebView for correct scaling ---
                            it.settings.javaScriptEnabled = true
                            it.settings.useWideViewPort = true      // honor viewport meta
                            it.settings.loadWithOverviewMode = false // prevent automatic scaling
                            it.setInitialScale(100)                 // start at 100%
                            it.settings.setSupportZoom(false)
                            it.settings.builtInZoomControls = false

                            Log.d(
                                "SmartPlayer", "WebView settings applied: " +
                                        "useWideViewPort=${it.settings.useWideViewPort}, " +
                                        "loadWithOverviewMode=${it.settings.loadWithOverviewMode}, " +
                                        "initialScale=${it.scale}"
                            )

                            it.webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                                    Log.d(
                                        "WebViewConsole",
                                        "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"
                                    )
                                    return true
                                }
                            }

                            webView.value = it
                            webViewCaptured = true
                            onWebViewReady(it)
                            Log.d("SmartPlayer", "WebView found and configured")

                            // If container size is already known, inject viewport now
                            if (currentSize.width > 0) {
                                injectDynamicViewport(it, currentSize.width)
                                it.evaluateJavascript(
                                    "window.dispatchEvent(new Event('resize'))",
                                    null
                                )
                            } else {
                                Log.d(
                                    "SmartPlayer",
                                    "Container size not yet known, will inject later"
                                )
                            }
                        } ?: Log.e("SmartPlayer", "WebView not found in hierarchy")
                    }
                }

                videoId?.let {
                    Log.d("SmartPlayer", "Loading video: $it")
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        }
        youTubePlayerView.addYouTubePlayerListener(listener)
        onDispose { youTubePlayerView.removeYouTubePlayerListener(listener) }
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
                addView(
                    youTubePlayerView, ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        },
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newSize = coordinates.size
                if (currentSize != newSize) {
                    Log.d(
                        "SmartPlayer",
                        "Container size updated: ${newSize.width}x${newSize.height}"
                    )
                    currentSize = newSize
                }
            }
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