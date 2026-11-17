package com.example.jazzlibraryktroomjpcompose.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun LineIndicator(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val segmentPx = widthPx / pageCount

        // Preferred line width; shrink so lines never touch (60% line / 40% gap).
        // 0.5.dp floor keeps them visible; below that they fade but stay correct.
        val lineWidth = with(density) {
            val preferredPx = 2.dp.toPx()
            val maxPx = segmentPx * 0.6f
            minOf(preferredPx, maxPx).toDp()
        }.coerceAtLeast(0.5.dp)

        // Same height as the old dots (6 dp active / 4 dp inactive).
        val lineHeight = 6.dp

        // Same line thickness for all items keeps SpaceEvenly positions exact
        // (no drift for the active item).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(pageCount, widthPx) {
                    detectTapGestures { offset ->
                        if (widthPx > 0f) {
                            val idx = ((offset.x / widthPx) * pageCount)
                                .toInt()
                                .coerceIn(0, pageCount - 1)
                            onPageSelected(idx)
                        }
                    }
                }
                .pointerInput(pageCount, widthPx) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (widthPx > 0f) {
                                val idx = ((offset.x / widthPx) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                                onPageSelected(idx)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (widthPx > 0f) {
                                val idx = ((change.position.x / widthPx) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                                if (idx != currentPage) onPageSelected(idx)
                            }
                        }
                    )
                },
            // ← THIS is the key change vs. dots:
            //   dots were Arrangement.Center; lines spread across the whole width.
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { i ->
                val isActive = i == currentPage
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(lineWidth)
                        .height(lineHeight)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}