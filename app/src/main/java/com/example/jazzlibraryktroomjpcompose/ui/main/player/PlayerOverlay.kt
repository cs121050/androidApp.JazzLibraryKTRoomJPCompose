package com.example.jazzlibraryktroomjpcompose.ui.main.player

import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import kotlinx.coroutines.launch
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerStableState

@Composable
fun PlayerOverlay(
    playerViewModel: PlayerViewModel,
    isFullscreen: Boolean,
    activeCardRelativePosition: IntOffset?,
    activeCardSize: IntSize?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stableState by playerViewModel.stableState.collectAsState()
    // Dynamic state collected here – only this composable recomposes on position updates
    val dynamicState by playerViewModel.dynamicState.collectAsState()

    val playerVisibilityModifier = if (stableState.isVisible) {
        Modifier   // will be replaced by specific mode modifier
    } else {
        Modifier.size(0.dp)   // completely invisible, but still in tree
    }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffsetY = remember { Animatable(0f) }

    LaunchedEffect(stableState.isInMiniMode) {
        if (stableState.isInMiniMode) dragOffsetY.snapTo(0f)
    }

    val topTapThresholdPx = with(density) { 80.dp.toPx() }

    when {
        isFullscreen -> {
            // Fullscreen player takes whole screen
            FullscreenPlayerContent(
                playerViewModel = playerViewModel,
                stableState = stableState,
                onClose = onClose,
                modifier = modifier.fillMaxSize()
            )
        }
        stableState.isInMiniMode -> {
            Log.d("PlayerOverlay", "Rendering player: isMiniMode=${stableState.isInMiniMode}, playerInstanceId=${stableState.playerInstanceId}, currentVideoId=${stableState.currentVideoId}")
            // Mini player: wrap in a Box that aligns to bottom‑end
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                MiniPlayerContent(
                    playerViewModel = playerViewModel,
                    stableState = stableState,
                    modifier = Modifier
                        .size(width = 235.dp, height = 200.dp)
                        .padding(bottom = 6.dp, end = 6.dp)
                        .zIndex(5f)
                )
            }
        }
        else -> {
            // Attached to a card
            activeCardRelativePosition?.let { pos ->
                activeCardSize?.let { size ->
                    Box(
                        modifier = modifier
                            .size(
                                width = with(density) { size.width.toDp() },
                                height = with(density) { size.height.toDp() }
                            )
                            .offset { IntOffset(pos.x, pos.y) }
                            .zIndex(5f)
                    ) {
                        AttachedPlayerContent(
                            playerViewModel = playerViewModel,
                            stableState = stableState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenPlayerContent(
    playerViewModel: PlayerViewModel,
    stableState: PlayerStableState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topTapThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (offset.y < topTapThresholdPx) {
                        // Show bars (implement as needed)
                    }
                }
            }
    ) {
        SmartYoutubePlayerHost(
            key = stableState.playerInstanceId,
            videoId = stableState.currentVideoId,
            isFullscreen = true,
            isMiniMode = false,
            onPlayerReady = { player -> playerViewModel.setPlayer(player) },
            onWebViewReady = { webView ->
                webView.post {
                    webView.setPadding(0, 0, 0, 0)
                    webView.isScrollContainer = false
                    webView.isVerticalScrollBarEnabled = false
                    webView.isHorizontalScrollBarEnabled = false
                    webView.setInitialScale(100)
                    webView.layoutParams = webView.layoutParams.apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    webView.requestLayout()
                }
            },
            onVideoEnded = { playerViewModel.nextVideo(startInMiniMode = true) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )

        FullscreenControlsColumn(
            onShare = { /* TODO */ },
            onCast = { /* TODO */ },
            onBack = { /* TODO */ },
            onPrevious = { playerViewModel.previousVideo() },
            onNext = { playerViewModel.nextVideo() },
            onClose = onClose,
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .zIndex(10f)
        )
    }
}

@Composable
private fun MiniPlayerContent(
    playerViewModel: PlayerViewModel,
    stableState: PlayerStableState,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SmartYoutubePlayerHost(
            key = stableState.playerInstanceId,
            videoId = stableState.currentVideoId,
            isFullscreen = false,
            isMiniMode = true,
            onPlayerReady = { player -> playerViewModel.setPlayer(player) },
            onWebViewReady = { webView ->
                webView.post {
                    webView.setPadding(0, 0, 0, 0)
                    webView.isScrollContainer = false
                    webView.isVerticalScrollBarEnabled = false
                    webView.isHorizontalScrollBarEnabled = false
                    webView.setInitialScale(100)
                    webView.layoutParams = webView.layoutParams.apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    webView.requestLayout()
                }
            },
            onVideoEnded = { playerViewModel.nextVideo(startInMiniMode = true) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun AttachedPlayerContent(
    playerViewModel: PlayerViewModel,
    stableState: PlayerStableState,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SmartYoutubePlayerHost(
            key = stableState.playerInstanceId,
            videoId = stableState.currentVideoId,
            isFullscreen = false,
            isMiniMode = false,
            onPlayerReady = { player -> playerViewModel.setPlayer(player) },
            onWebViewReady = { webView ->
                webView.post {
                    webView.setPadding(0, 0, 0, 0)
                    webView.isScrollContainer = false
                    webView.isVerticalScrollBarEnabled = false
                    webView.isHorizontalScrollBarEnabled = false
                    webView.setInitialScale(100)
                    webView.layoutParams = webView.layoutParams.apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    webView.requestLayout()
                }
            },
            onVideoEnded = { playerViewModel.nextVideo(startInMiniMode = true) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun FullscreenControlsColumn(
    onShare: () -> Unit,
    onCast: () -> Unit,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onCast) {
            Icon(Icons.Default.Cast, contentDescription = "Cast", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onNext) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}

// Temporary placeholder for PlayerStableState – you already have this class.
// Remove this if your actual PlayerStableState is already defined.
// This is only to make the file self‑contained for compilation.
