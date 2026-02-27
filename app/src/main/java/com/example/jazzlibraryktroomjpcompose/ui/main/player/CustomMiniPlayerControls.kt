package com.example.jazzlibraryktroomjpcompose.ui.main.player


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun CustomMiniPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    autoHideDelay: Long = 6000L,
    doubleTapSeekAmount: Long = 10000,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Always use the latest values inside the gesture detector
    val currentPositionState by rememberUpdatedState(currentPosition)
    val durationState by rememberUpdatedState(duration)

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "controls_alpha"
    )

    // Auto-hide timer
    LaunchedEffect(key1 = isVisible) {
        if (isVisible) {
            delay(autoHideDelay)
            isVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isVisible = true },
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        val tapX = offset.x

                        if (tapX < screenWidth / 2) {
                            // Left side – rewind
                            val newPosition = (currentPositionState - doubleTapSeekAmount).coerceAtLeast(0)
                            onSeek(newPosition)
                        } else {
                            // Right side – forward
                            val newPosition = (currentPositionState + doubleTapSeekAmount)
                                .coerceAtMost(durationState)
                            onSeek(newPosition)
                        }
                        isVisible = true
                    }
                )
            }
    ) {
        // Time display at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .alpha(alpha)
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp)
                    .shadow(4.dp)
            )

            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp)
                    .shadow(4.dp)
            )
        }

        // Buttons at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 0.dp)
                .alpha(alpha),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(2.dp, shape = MaterialTheme.shapes.small)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable(
                        onClick = {
                            isVisible = true
                            onLeftTap()
                        },
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(2.dp, shape = MaterialTheme.shapes.small)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable(
                        onClick = {
                            isVisible = true
                            onRightTap()
                        },
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Subtle double‑tap hints
        if (isVisible) {
            Text(
                text = "« 10s",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
            Text(
                text = "10s »",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }
    }
}

// 🔽 Add this helper function (can be placed at the bottom of the file)
private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%d:%02d", minutes, seconds)
}