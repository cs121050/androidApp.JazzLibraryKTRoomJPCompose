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
    val webView = remember { mutableStateOf<WebView?>(null) }
    var webViewCaptured by remember { mutableStateOf(false) }

    // Log mode changes
    LaunchedEffect(isFullscreen, isMiniMode) {
        Log.d("SmartPlayer", "Mode changed: isFullscreen=$isFullscreen, isMiniMode=$isMiniMode")
    }

    // Reapply UI mode when mode changes or webView becomes available
    LaunchedEffect(isFullscreen, isMiniMode, webView.value) {
        val view = webView.value
        if (view != null) {
            Log.d("SmartPlayer", "Applying UI mode after mode change (delay 100ms)")
            delay(100)
            applyUIMode(view, isFullscreen, isMiniMode)
        } else {
            Log.d("SmartPlayer", "WebView not yet available for mode change")
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

                // Capture WebView only once
                if (!webViewCaptured) {
                    findWebView(youTubePlayerView) { capturedWebView ->
                        capturedWebView?.let {
                            it.settings.javaScriptEnabled = true
                            it.webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                                    Log.d("WebViewConsole", "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
                                    return true
                                }
                            }

                            webView.value = it
                            webViewCaptured = true
                            onWebViewReady(it)
                            Log.d("SmartPlayer", "WebView found and configured: $it")

                            // Apply initial UI mode
                            applyUIMode(it, isFullscreen, isMiniMode)
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
private fun applyUIMode(webView: WebView, isFullscreen: Boolean, isMiniMode: Boolean) {
    Log.d("SmartPlayer", "applyUIMode called: isFullscreen=$isFullscreen, isMiniMode=$isMiniMode")

    val script = when {
        isFullscreen -> {
            Log.d("SmartPlayer", "Applying fullscreen style")
            """
            (function() {
                console.log('[Fullscreen] Applying fullscreen style');
                var styleId = '__ytFullscreenStyle';
                var existing = document.getElementById(styleId);
                if (existing) existing.remove();
                var style = document.createElement('style');
                style.id = styleId;
                style.innerHTML = `
                    html, body {
                        margin: 0 !important;
                        padding: 0 !important;
                        width: 100% !important;
                        height: 100% !important;
                        overflow: hidden !important;
                    }
                    .html5-video-container, #player, .ytp-player-content {
                        width: 100% !important;
                        height: 100% !important;
                        overflow: hidden !important;
                    }
                    video, .html5-main-video {
                        width: 100% !important;
                        height: 100% !important;
                        object-fit: fill !important;
                    }
                `;
                document.head.appendChild(style);
                document.body.style.overflow = 'hidden';
                window.__ytFullscreenCleanup = function() {
                    var s = document.getElementById(styleId);
                    if (s) s.remove();
                    document.body.style.overflow = '';
                    console.log('[Fullscreen] Cleaned up');
                };
                console.log('[Fullscreen] Style applied');
            })();
            """.trimIndent()
        }
        isMiniMode -> {
            Log.d("SmartPlayer", "Applying mini mode style with improved cleanup")
            """
            (function() {
                console.log('[Mini] Starting mini mode');
                var styleId = '__ytMiniStyle';
                var observer = window.__ytMiniObserver;
                
                // Disconnect any existing observer
                if (observer) {
                    observer.disconnect();
                    console.log('[Mini] Disconnected previous observer');
                }
                
                // Set active flag
                window.__ytMiniActive = true;
                console.log('[Mini] Active flag = true');
                
                function applyMiniStyles() {
                    if (!window.__ytMiniActive) {
                        console.log('[Mini] Skipping style application – inactive');
                        return;
                    }
                    if (window.__ytMiniStylesApplied) {
                        console.log('[Mini] Styles already applied, skipping');
                        return;
                    }
                    console.log('[Mini] Applying CSS styles now');
                    var existing = document.getElementById(styleId);
                    if (existing) existing.remove();
                    var style = document.createElement('style');
                    style.id = styleId;
                    style.innerHTML = `
                        /* Scale top and bottom bars */
                        .ytp-chrome-top, .ytp-chrome-bottom {
                            transform: scale(0.65) !important;
                            transform-origin: top center !important;
                        }
                        /* Do not scale video */
                        video, .html5-main-video {
                            transform: none !important;
                        }
                        /* Progress bar scaling */
                        .ytp-progress-bar-container {
                            transform: scaleY(0.7) !important;
                            transform-origin: bottom !important;
                        }
                        /* Make title red for debugging, and smaller */
                        .ytp-title-text {
                            color: red !important;
                            font-size: 12px !important;
                        }
                    `;
                    document.head.appendChild(style);
                    window.__ytMiniStylesApplied = true;
                    console.log('[Mini] Style element added – title should be red');
                }
            
                // If the elements already exist, apply now
                if (document.querySelector('.ytp-chrome-top')) {
                    applyMiniStyles();
                } else {
                    // Wait for them using MutationObserver
                    observer = new MutationObserver(function(mutations, obs) {
                        console.log('[Mini] MutationObserver triggered, checking for .ytp-chrome-top');
                        if (document.querySelector('.ytp-chrome-top')) {
                            console.log('[Mini] .ytp-chrome-top found, active flag = ' + window.__ytMiniActive);
                            applyMiniStyles();
                            obs.disconnect();
                            console.log('[Mini] Observer disconnected after applying styles');
                        }
                    });
                    observer.observe(document.body, { childList: true, subtree: true });
                    window.__ytMiniObserver = observer;
                    console.log('[Mini] Waiting for .ytp-chrome-top...');
                }
                
                window.__ytMiniCleanup = function() {
                    console.log('[Mini] Cleaning up');
                    // Deactivate first to prevent any pending observer callbacks from applying
                    window.__ytMiniActive = false;
                    console.log('[Mini] Active flag = false');
                    if (window.__ytMiniObserver) {
                        window.__ytMiniObserver.disconnect();
                        delete window.__ytMiniObserver;
                        console.log('[Mini] Observer disconnected');
                    }
                    var s = document.getElementById('__ytMiniStyle');
                    if (s) {
                        s.remove();
                        console.log('[Mini] Style element removed');
                    }
                    // Explicitly reset title font size in case something went wrong
                    var title = document.querySelector('.ytp-title-text');
                    if (title) {
                        title.style.fontSize = '';
                        title.style.color = '';
                        console.log('[Mini] Reset title font size and color');
                    }
                    delete window.__ytMiniStylesApplied;
                    console.log('[Mini] Cleanup complete');
                };
            })();
            """.trimIndent()
        }
        else -> {
            Log.d("SmartPlayer", "Cleaning up styles (normal mode)")
            """
            (function() {
                console.log('[Cleanup] Cleaning up styles');
                if (window.__ytFullscreenCleanup) window.__ytFullscreenCleanup();
                if (window.__ytMiniCleanup) window.__ytMiniCleanup();
                console.log('[Cleanup] Done');
            })();
            """.trimIndent()
        }
    }

    webView.evaluateJavascript(script) { result ->
        Log.d("SmartPlayer", "JavaScript execution result: $result")
    }
}