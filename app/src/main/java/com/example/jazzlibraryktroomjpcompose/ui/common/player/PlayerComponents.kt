package com.example.jazzlibraryktroomjpcompose.ui.common.player

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel

@Composable
fun FullscreenControlsColumn(
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

@Composable
fun PlayerCardVisibilityMonitor(
    listState: LazyListState,
    activeCardId: String?,
    playerViewModel: PlayerViewModel
) {
    LaunchedEffect(listState, activeCardId) {
        Log.d("VisibilityMonitor", "🚀 Monitor started. activeCardId = $activeCardId")

        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val visibleKeys = visibleItems.map { it.key.toString() }
                val isVisible = activeCardId != null && visibleKeys.contains(activeCardId)

                // Log current player state (without referencing missing isCardVisible)
                val currentUiState = playerViewModel.uiState.value
                Log.d("VisibilityMonitor", "🎮 Player state: isVisible=${currentUiState.isVisible}, isInMiniMode=${currentUiState.isInMiniMode}, activeCardId=${currentUiState.activeCardId}")

                // Always call ViewModel method – it internally checks if visibility changed
                playerViewModel.onCardVisibilityChanged(isVisible)
            }
    }
}