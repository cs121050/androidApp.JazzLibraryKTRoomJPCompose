// YouTubeLikeBottomSheet.kt
package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState as rememberHorizontalScrollState

@Composable
fun YouTubeLikeBottomSheet(
    viewModel: MainViewModel,
    uiState: MainUiState,
    filterState: FilterState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Collect states
    val sheetState by viewModel.bottomSheetState.collectAsState()
    val sheetProgress by viewModel.bottomSheetProgress.collectAsState()

    // Use actual screen dimensions
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Calculate heights
    val maxHeightPx = screenHeightPx * 0.8f // 80% of screen
    val halfHeightPx = screenHeightPx * 0.5f // 50% of screen

    // Track drag state
    var dragStartHeight by remember { mutableStateOf(0f) }
    var dragVelocity by remember { mutableStateOf(0f) }
    var lastDragTime by remember { mutableStateOf(0L) }

    // Separate shadow animation state
    val isShadowVisible = sheetState != BottomSheetState.HIDDEN

    // Independent shadow animation - fades in/out
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isShadowVisible) 0.4f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        ),
        label = "shadowAlpha"
    )

    // The shadow overlay - completely independent animation
    AnimatedVisibility(
        visible = isShadowVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = shadowAlpha))
                .pointerInput(Unit) {
                    detectTapGestures {
                        // Close on background tap
                        coroutineScope.launch {
                            viewModel.setBottomSheetState(BottomSheetState.HIDDEN)
                        }
                    }
                }
        )
    }

    // The sheet itself - separate animation
    AnimatedVisibility(
        visible = sheetState != BottomSheetState.HIDDEN,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        coroutineScope.launch {
                            viewModel.setBottomSheetState(BottomSheetState.HIDDEN)
                        }
                    }
                }
        ) {
            // The sheet itself with animated height
            val targetHeight = when (sheetState) {
                BottomSheetState.HIDDEN -> 0f // Shouldn't reach here due to AnimatedVisibility
                BottomSheetState.HALF_EXPANDED -> halfHeightPx
                BottomSheetState.EXPANDED -> maxHeightPx
            }

            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "sheetHeight"
            )

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(with(density) { animatedHeight.toDp() })
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartHeight = animatedHeight
                                dragVelocity = 0f
                                lastDragTime = System.currentTimeMillis()
                            },
                            onDragEnd = {
                                val progress = animatedHeight / maxHeightPx

                                // Logic for dragging from expanded
                                val targetState = when {
                                    // Fast swipe down - close
                                    dragVelocity > 1000f -> BottomSheetState.HIDDEN
                                    // Fast swipe up - expand
                                    dragVelocity < -1000f -> BottomSheetState.EXPANDED
                                    // Position-based with consideration of starting state
                                    progress > 0.65f -> BottomSheetState.EXPANDED
                                    progress > 0.35f -> BottomSheetState.HALF_EXPANDED
                                    else -> BottomSheetState.HIDDEN
                                }

                                // Animate to the target state
                                coroutineScope.launch {
                                    viewModel.setBottomSheetState(targetState)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                // Calculate velocity
                                val currentTime = System.currentTimeMillis()
                                val timeDelta = (currentTime - lastDragTime).coerceAtLeast(1L)
                                dragVelocity = dragAmount.y / timeDelta * 1000
                                lastDragTime = currentTime

                                // Calculate new height with bounds
                                val newHeight = (dragStartHeight - dragAmount.y)
                                    .coerceIn(0f, maxHeightPx)

                                // Update progress for smooth dragging
                                viewModel.updateBottomSheetProgress(newHeight / maxHeightPx)
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Drag handle area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        dragStartHeight = animatedHeight
                                        dragVelocity = 0f
                                        lastDragTime = System.currentTimeMillis()
                                    },
                                    onDragEnd = {
                                        val progress = animatedHeight / maxHeightPx

                                        // Handle swipe directions
                                        val targetState = when {
                                            // SWIPE UP from half-expanded -> EXPANDED
                                            dragVelocity < 0 && sheetState == BottomSheetState.HALF_EXPANDED -> {
                                                BottomSheetState.EXPANDED
                                            }
                                            // SWIPE DOWN from half-expanded -> close
                                            dragVelocity > 0 && sheetState == BottomSheetState.HALF_EXPANDED -> {
                                                BottomSheetState.HIDDEN
                                            }
                                            // SWIPE UP from expanded -> go to half-expanded
                                            dragVelocity < 0 && sheetState == BottomSheetState.EXPANDED -> {
                                                BottomSheetState.HALF_EXPANDED
                                            }
                                            // SWIPE DOWN from expanded -> go to half-expanded
                                            dragVelocity > 0 && sheetState == BottomSheetState.EXPANDED -> {
                                                BottomSheetState.HALF_EXPANDED
                                            }
                                            // No velocity or very slow drag - use position-based logic
                                            else -> {
                                                when {
                                                    progress > 0.65f -> BottomSheetState.EXPANDED
                                                    progress > 0.35f -> BottomSheetState.HALF_EXPANDED
                                                    else -> BottomSheetState.HIDDEN
                                                }
                                            }
                                        }

                                        coroutineScope.launch {
                                            viewModel.setBottomSheetState(targetState)
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        // Calculate velocity (negative = up, positive = down)
                                        val currentTime = System.currentTimeMillis()
                                        val timeDelta =
                                            (currentTime - lastDragTime).coerceAtLeast(1L)
                                        dragVelocity = dragAmount.y / timeDelta * 1000
                                        lastDragTime = currentTime

                                        // Calculate new height
                                        val newHeight = (dragStartHeight - dragAmount.y)
                                            .coerceIn(0f, maxHeightPx)

                                        viewModel.updateBottomSheetProgress(newHeight / maxHeightPx)
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        // TAP GESTURE: Toggle between half-expanded and expanded
                                        coroutineScope.launch {
                                            when (sheetState) {
                                                BottomSheetState.HALF_EXPANDED ->
                                                    viewModel.setBottomSheetState(BottomSheetState.EXPANDED)

                                                BottomSheetState.EXPANDED ->
                                                    viewModel.setBottomSheetState(BottomSheetState.HALF_EXPANDED)

                                                else -> {}
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Visual drag handle
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Content area
                    YouTubeBottomSheetContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        filterState = filterState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubeBottomSheetContent(
    viewModel: MainViewModel,
    uiState: MainUiState,
    filterState: FilterState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Instrument Chip Group
        ChipGroupSection(
            title = "Instruments",
            categoryId = FilterPath.CATEGORY_INSTRUMENT,
            items = if (filterState.currentFilterPath.isEmpty()) {
                uiState.allInstruments
            } else {
                uiState.availableInstruments
            },
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            }
        )

        // Artist Chip Group
        ChipGroupSection(
            title = "Artists",
            categoryId = FilterPath.CATEGORY_ARTIST,
            items = uiState.availableArtists,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            }
        )

        // Duration Chip Group
        ChipGroupSection(
            title = "Durations",
            categoryId = FilterPath.CATEGORY_DURATION,
            items = uiState.availableDurations,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            }
        )

        // Type Chip Group
        ChipGroupSection(
            title = "Types",
            categoryId = FilterPath.CATEGORY_TYPE,
            items = uiState.availableTypes,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ChipGroupSection(
    title: String,
    categoryId: Int,
    items: List<Any>,
    currentFilterPath: List<FilterPath>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Horizontal scrollable chip group
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .horizontalScroll(rememberHorizontalScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                when (item) {
                    is com.example.jazzlibraryktroomjpcompose.domain.models.Instrument -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChipItem(
                            id = item.id,
                            name = item.name,
                            categoryId = categoryId,
                            isSelected = isSelected,
                            onSelectedChange = { selected ->
                                onChipSelected(categoryId, item.id, item.name, selected)
                            },
                            additionalInfo = "ID: ${item.id}",
                            modifier = Modifier.height(48.dp)
                        )
                    }
                    is com.example.jazzlibraryktroomjpcompose.domain.models.Artist -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChipItem(
                            id = item.id,
                            name = item.fullName,
                            categoryId = categoryId,
                            isSelected = isSelected,
                            onSelectedChange = { selected ->
                                onChipSelected(categoryId, item.id, item.fullName, selected)
                            },
                            additionalInfo = "ID: ${item.id}\nInstrument: ${item.instrumentId}\nRank: ${item.rank}",
                            modifier = Modifier.height(48.dp)
                        )
                    }
                    is com.example.jazzlibraryktroomjpcompose.domain.models.Duration -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChipItem(
                            id = item.id,
                            name = item.name,
                            categoryId = categoryId,
                            isSelected = isSelected,
                            onSelectedChange = { selected ->
                                onChipSelected(categoryId, item.id, item.name, selected)
                            },
                            additionalInfo = "ID: ${item.id}",
                            modifier = Modifier.height(48.dp)
                        )
                    }
                    is com.example.jazzlibraryktroomjpcompose.domain.models.Type -> {
                        val isSelected = currentFilterPath.any {
                            it.categoryId == categoryId && it.entityId == item.id
                        }

                        FilterChipItem(
                            id = item.id,
                            name = item.name,
                            categoryId = categoryId,
                            isSelected = isSelected,
                            onSelectedChange = { selected ->
                                onChipSelected(categoryId, item.id, item.name, selected)
                            },
                            additionalInfo = "ID: ${item.id}",
                            modifier = Modifier.height(48.dp)
                        )
                    }
                    else -> {
                        // Fallback for unknown types
                        Text(
                            text = "Unknown item type",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${items.size} options available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun FilterChipItem(
    id: Int,
    name: String,
    categoryId: Int,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    additionalInfo: String? = null,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelectedChange(!isSelected) },
        label = {
            Column(
                modifier = Modifier.widthIn(min = 80.dp, max = 120.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                additionalInfo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            borderWidth = if (isSelected) 2.dp else 1.dp
        )
    )
}