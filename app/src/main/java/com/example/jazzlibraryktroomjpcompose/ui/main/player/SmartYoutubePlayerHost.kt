package com.example.jazzlibraryktroomjpcompose.ui.main.player


import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmartYoutubePlayerHost(
    key: Any,
    videoId: String?,
    isMiniMode: Boolean,
    onPlayerReady: (YouTubePlayer) -> Unit,
    onWebViewReady: (WebView) -> Unit,      // 👈 new callback
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val youTubePlayerView = remember(key) { YouTubePlayerView(context) }
    val currentPlayer = remember { mutableStateOf<YouTubePlayer?>(null) }
    val webView = remember { mutableStateOf<WebView?>(null) }

    // Attach lifecycle
    DisposableEffect(lifecycleOwner, youTubePlayerView) {
        lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    // Initialize player and capture WebView
    DisposableEffect(youTubePlayerView) {
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                currentPlayer.value = youTubePlayer
                onPlayerReady(youTubePlayer)

                // Capture WebView
                try {
                    val webViewField = youTubePlayerView.javaClass.getDeclaredField("webView")
                    webViewField.isAccessible = true
                    val capturedWebView = webViewField.get(youTubePlayerView) as? WebView
                    webView.value = capturedWebView
                    capturedWebView?.let { onWebViewReady(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }

                // Apply initial UI mode
                coroutineScope.launch {
                    delay(500)
                    webView.value?.let { applyUIMode(it, isMiniMode) }
                }
            }
        }
        youTubePlayerView.addYouTubePlayerListener(listener)
        onDispose {
            youTubePlayerView.removeYouTubePlayerListener(listener)
        }
    }

    // Handle video ID changes
    LaunchedEffect(videoId, currentPlayer.value) {
        val player = currentPlayer.value
        if (player != null && videoId != null) {
            player.loadVideo(videoId, 0f)
        }
    }

    // Handle mode changes
    LaunchedEffect(isMiniMode, webView.value) {
        val view = webView.value
        if (view != null) {
            delay(100)
            applyUIMode(view, isMiniMode)
        }
    }

    AndroidView(
        factory = { youTubePlayerView },
        modifier = modifier
    )
}

private fun applyUIMode(webView: WebView, isMiniMode: Boolean) {
    webView.post {
        val script = if (isMiniMode) {
            """
            javascript:(function() {
                // Clean up any previous instance
                if (window.__ytCleanup) {
                    window.__ytCleanup();
                }

                // ---------- Hide Function (always uses current video) ----------
                function hideControls() {
                    var controls = document.querySelectorAll('[class*="ytp-"]:not(video):not(.html5-video-container)');
                    for (var i = 0; i < controls.length; i++) {
                        var el = controls[i];
                        el.style.setProperty('display', 'none', 'important');
                        el.style.setProperty('opacity', '0', 'important');
                        el.style.setProperty('visibility', 'hidden', 'important');
                        el.style.setProperty('pointer-events', 'none', 'important');
                    }
                }

                // ---------- Burst Mode: run hideControls frequently for a short period ----------
                var burstTimer = null;
                var burstCount = 0;
                function startBurstMode() {
                    if (burstTimer) clearInterval(burstTimer);
                    burstCount = 0;
                    burstTimer = setInterval(function() {
                        hideControls();
                        burstCount++;
                        // Run for about 500ms (50 times at 10ms interval)
                        if (burstCount >= 50) {
                            clearInterval(burstTimer);
                            burstTimer = null;
                        }
                    }, 10);
                }

                // ---------- 1. CSS (always active, zero CPU) ----------
                var style = document.createElement('style');
                style.id = '__ytMiniStyle';
                style.innerHTML = `
                    [class*="ytp-"]:not(video):not(.html5-video-container) {
                        display: none !important;
                        opacity: 0 !important;
                        visibility: hidden !important;
                        pointer-events: none !important;
                    }
                    video, .html5-main-video, .html5-video-container {
                        width: 100% !important;
                        height: 100% !important;
                        object-fit: cover !important;
                    }
                `;
                document.head.appendChild(style);

                // ---------- 2. MutationObserver (catches new DOM nodes) ----------
                var observer = new MutationObserver(function(mutations) {
                    var needsHide = false;
                    for (var i = 0; i < mutations.length; i++) {
                        if (mutations[i].addedNodes.length > 0) {
                            needsHide = true;
                            break;
                        }
                    }
                    if (needsHide) {
                        hideControls();
                        // Also trigger burst mode because new nodes might appear in rapid succession
                        startBurstMode();
                    }
                });
                observer.observe(document.body, { childList: true, subtree: true });

                // ---------- 3. Video event listeners (play, pause, seek, etc.) ----------
                var video = document.querySelector('video');
                if (video) {
                    var events = ['play', 'pause', 'seeked', 'playing', 'waiting', 'canplay', 'loadeddata'];
                    for (var i = 0; i < events.length; i++) {
                        video.addEventListener(events[i], function() {
                            hideControls();           // immediate hide
                            startBurstMode();         // then burst to catch any delayed UI
                        });
                    }
                }

                // ---------- 4. Touch events on the whole document (catch any interaction) ----------
                document.addEventListener('touchstart', function() {
                    startBurstMode();
                }, { passive: true });
                document.addEventListener('touchend', function() {
                    startBurstMode();
                }, { passive: true });
                document.addEventListener('click', function() {
                    startBurstMode();
                });

                // ---------- 5. Initial hide (multiple passes to be safe) ----------
                hideControls();
                setTimeout(hideControls, 50);
                setTimeout(hideControls, 100);
                setTimeout(hideControls, 200);

                // ---------- 6. Cleanup function ----------
                window.__ytCleanup = function() {
                    if (burstTimer) clearInterval(burstTimer);
                    if (observer) observer.disconnect();
                    var style = document.getElementById('__ytMiniStyle');
                    if (style) style.remove();

                    // Remove event listeners (optional, but good practice)
                    if (video) {
                        var events = ['play', 'pause', 'seeked', 'playing', 'waiting', 'canplay', 'loadeddata'];
                        for (var i = 0; i < events.length; i++) {
                            video.removeEventListener(events[i], hideControls);
                        }
                    }

                    // Restore visibility for full mode
                    var controls = document.querySelectorAll('[class*="ytp-"]');
                    for (var i = 0; i < controls.length; i++) {
                        var el = controls[i];
                        el.style.display = '';
                        el.style.opacity = '';
                        el.style.visibility = '';
                        el.style.pointerEvents = '';
                    }

                    window.__ytCleanup = null;
                };

                console.log('✅ Mini mode activated with burst mode');
            })()
            """.trimIndent()
        } else {
            // Return to full mode
            """
            javascript:(function() {
                if (window.__ytCleanup) {
                    window.__ytCleanup();
                }
                console.log('✅ Full mode restored');
            })()
            """.trimIndent()
        }

        webView.loadUrl(script)
    }
}