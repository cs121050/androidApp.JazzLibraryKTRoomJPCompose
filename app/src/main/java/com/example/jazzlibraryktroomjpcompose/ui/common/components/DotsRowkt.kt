package com.example.jazzlibraryktroomjpcompose.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens

@Composable
fun DotsRow(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rowWidth by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                rowWidth = coordinates.size.width
            }
            .pointerInput(pageCount) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = offset.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex =
                                ((x / rowWidth) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                            onPageSelected(pageIndex)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = change.position.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex =
                                ((x / rowWidth) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                            if (pageIndex != currentPage) {
                                onPageSelected(pageIndex)
                            }
                        }
                    }
                )
            }
            .pointerInput(pageCount) {
                detectTapGestures { offset ->
                    if (rowWidth > 0 && pageCount > 0) {
                        val x = offset.x.coerceIn(0f, rowWidth.toFloat())
                        val pageIndex =
                            ((x / rowWidth) * pageCount)
                                .toInt()
                                .coerceIn(0, pageCount - 1)
                        onPageSelected(pageIndex)
                    }
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            Box(
                modifier = Modifier
                    .size(if (i == currentPage) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
            )
            if (i != pageCount - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun FastScrollingDotsRow(
    pageCount: Int,
    currentPage: Int,
    onSwitchToAlphabeticalAndScrollTo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rowWidth by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                rowWidth = layoutCoordinates.size.width
            }
            .pointerInput(pageCount) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = offset.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex =
                                ((x / rowWidth) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                            onSwitchToAlphabeticalAndScrollTo(pageIndex)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = change.position.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex =
                                ((x / rowWidth) * pageCount)
                                    .toInt()
                                    .coerceIn(0, pageCount - 1)
                            if (pageIndex != currentPage) {
                                onSwitchToAlphabeticalAndScrollTo(pageIndex)
                            }
                        }
                    }
                )
            }
            .pointerInput(pageCount) {
                detectTapGestures { offset ->
                    if (rowWidth > 0 && pageCount > 0) {
                        val x = offset.x.coerceIn(0f, rowWidth.toFloat())
                        val pageIndex =
                            ((x / rowWidth) * pageCount)
                                .toInt()
                                .coerceIn(0, pageCount - 1)
                        onSwitchToAlphabeticalAndScrollTo(pageIndex)
                    }
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            Box(
                modifier = Modifier
                    .size(if (i == currentPage) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
            )
            if (i != pageCount - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}