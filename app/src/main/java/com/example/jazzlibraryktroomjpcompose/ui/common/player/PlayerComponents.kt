package com.example.jazzlibraryktroomjpcompose.ui.common.player

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onClose: () -> Unit,
    onBack: () -> Unit,
    onCast: () -> Unit,
    onShare: () -> Unit,
    toBrowser: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    @Composable
    fun Control(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
        Box(
            modifier = Modifier.size(32.dp).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, desc, Modifier.size(25.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }

    Column(
        modifier = modifier
            .width(36.dp)
            .background(MaterialTheme.colorScheme.onSurface),
        verticalArrangement = Arrangement.SpaceEvenly,

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Control(Icons.Default.Close, "Close", onClose)
        Control(Icons.Default.ArrowBack, "Back", onBack)
        Control(Icons.Default.Cast, "Cast", onCast)
        Control(Icons.Default.Share, "Share", onShare)
        Control(Icons.Default.Language, "Internet", toBrowser)
        Control(Icons.Default.SkipPrevious, "Previous video", onPrevious)
        Control(Icons.Default.SkipNext, "Next video", onNext)
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