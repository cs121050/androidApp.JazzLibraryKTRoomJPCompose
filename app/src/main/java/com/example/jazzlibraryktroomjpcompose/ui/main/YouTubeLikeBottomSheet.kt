package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Use actual screen dimensions
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Calculate heights
    val maxHeightPx = screenHeightPx * 0.8f // 80% of screen
    val halfHeightPx = screenHeightPx * 0.55f // 50% of screen

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
                            .height(Dimens.pathChipHeight)
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
                            Spacer(modifier = Modifier.height(Dimens.commonSpacing))
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
        verticalArrangement = Arrangement.spacedBy(Dimens.largeSpacing)
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        // Instrument Chip Group
        ChipGroupSection(
            title = "Instruments",
            categoryId = FilterPath.CATEGORY_INSTRUMENT,
            items = uiState.availableInstruments,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            },
            maxHeight = 200.dp
        )

        // Artist Chip Group
        PaginatedArtistChipGroupSection(
            title = "Artists",
            items = uiState.availableArtists,
            selectedItemIds = filterState.currentFilterPath
                .filter { it.categoryId == FilterPath.CATEGORY_ARTIST }
                .map { it.entityId }
                .toSet(),
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            },
            maxHeight = 200.dp
        )

        // Duration Chip Group
        ChipGroupSection(
            title = "Durations",
            categoryId = FilterPath.CATEGORY_DURATION,
            items = uiState.availableDurations,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            },
            maxHeight = 100.dp
        )

        // Type Chip Group
        ChipGroupSection(
            title = "Types",
            categoryId = FilterPath.CATEGORY_TYPE,
            items = uiState.availableTypes,
            currentFilterPath = filterState.currentFilterPath,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
            },
            maxHeight = 100.dp
        )

        Spacer(modifier = Modifier.height(Dimens.largeSpacing))
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaginatedArtistChipGroupSection(
    title: String,
    items: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist>,
    selectedItemIds: Set<Int>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    maxHeight: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    // Get density in composable context
    val density = LocalDensity.current

    // Pagination state - start with 60 items - pagination starting number of chips
    var visibleItemCount by remember { mutableIntStateOf(80) }

    // Remember the scroll state for detecting when to load more
    val scrollState = rememberLazyListState()

    // AUTO-SCROLL: Track if we need to scroll to selection
    var hasScrolled by remember { mutableStateOf(false) }
    var selectedArtistPosition by remember { mutableFloatStateOf(0f) }
    var shouldScrollToSelection by remember { mutableStateOf(false) }

    // Create a derived state for visible items
    val visibleItems = remember(visibleItemCount, items) {
        items.take(visibleItemCount)
    }

    // AUTO-SCROLL: Reset scroll flag when selection changes
    LaunchedEffect(selectedItemIds) {
        hasScrolled = false
        shouldScrollToSelection = selectedItemIds.isNotEmpty()
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = Dimens.commonPadding)
        )

        Spacer(modifier = Modifier.height(Dimens.smallSpacing))

        // Use LazyColumn with FlowRow for natural chip layout
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight),
            userScrollEnabled = true
        ) {
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.commonPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallSpacing),
                    verticalArrangement = Arrangement.spacedBy(Dimens.smallSpacing)
                ) {
                    visibleItems.forEach { artist ->
                        val isSelected = selectedItemIds.contains(artist.id)
                        PaginatedArtistChip(
                            artist = artist,
                            isSelected = isSelected,
                            onPositionMeasured = { yPos, selected ->
                                if (selected && !hasScrolled) {
                                    selectedArtistPosition = yPos
                                    shouldScrollToSelection = true
                                }
                            },
                            onClick = {
                                onChipSelected(FilterPath.CATEGORY_ARTIST, artist.id, artist.fullName, !isSelected)
                            }
                        )
                    }
                }
            }
        }
    }

//    // AUTO-SCROLL: Scroll to selected artist (Simple version)
//    LaunchedEffect(shouldScrollToSelection, scrollState) {
//        if (shouldScrollToSelection && !hasScrolled) {
//            //delay(100)
//
//            // Calculate target scroll position
//            val currentScrollOffset = scrollState.firstVisibleItemScrollOffset
//            val targetScroll = (selectedArtistPosition - 100).toInt().coerceAtLeast(0)
//
//            // Scroll to the calculated position
//            scrollState.scrollToItem(
//                index = 0,  // We only have 1 item (the FlowRow)
//                scrollOffset = targetScroll
//            )
//
//            hasScrolled = true
//            shouldScrollToSelection = false
//        }
//    }

    //AUTO-SCROLL: Scroll to selected artist with smooth animation
    LaunchedEffect(shouldScrollToSelection, scrollState) {
        if (shouldScrollToSelection && !hasScrolled) {
            delay(30)

            val currentScrollOffset = scrollState.firstVisibleItemScrollOffset
            val targetScroll = (selectedArtistPosition - 100).toInt().coerceAtLeast(0)
            val scrollAmount = targetScroll - currentScrollOffset

            if (scrollAmount != 0) {
                // Smooth scroll animation
                val steps = 20
                val stepSize = scrollAmount / steps

                for (step in 1..steps) {
                    val newOffset = currentScrollOffset + (stepSize * step)
                    scrollState.scrollToItem(
                        index = 0,
                        scrollOffset = newOffset
                    )
                    delay(16) // ~60 FPS
                }
            }

            hasScrolled = true
            shouldScrollToSelection = false
        }
    }

    // for pagination of the artist chipgroup - Load more items when user scrolls near the bottom
// **FIXED: for pagination of the artist chipgroup - Simplified and more reliable**
    LaunchedEffect(scrollState, visibleItemCount, items.size, density) {
        // Convert Dp to Px once outside the collection
        val approxChipHeight = with(density) { 40.dp.toPx() }
        val approxSpacing = with(density) { Dimens.smallSpacing.toPx() }
        val approxRowHeight = approxChipHeight + approxSpacing

        // Use a simpler approach to detect when to load more
        while (visibleItemCount < items.size) {
            // Wait for scroll events
            snapshotFlow { scrollState.layoutInfo }
                .collect { layoutInfo ->
                    if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                        // Get current scroll position
                        val scrollOffset = scrollState.firstVisibleItemScrollOffset

                        // Estimate total content height (approximate)
                        // Each chip is about 40dp height, plus spacing
                        // Approximate total content height
                        val approxTotalHeight = approxRowHeight * (visibleItems.size / 4) // Approx 4 chips per row

                        // Load more when scrolled 70% down
                        if (approxTotalHeight > 0) {
                            val scrollPercentage = scrollOffset.toFloat() / approxTotalHeight

                            if (scrollPercentage > 0.2f) {
                                // Load more items
                                val newCount = minOf(visibleItemCount + 30, items.size)
                                if (newCount > visibleItemCount) {
                                    visibleItemCount = newCount
                                }
                                return@collect // Break and restart collection
                            }
                        }
                    }
                }

            // Small delay to prevent tight loop
            //delay(100)
        }
    }
}

@Composable
private fun ChipGroupSection(
    title: String,
    categoryId: Int,
    items: List<Any>,
    currentFilterPath: List<FilterPath>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    maxHeight: Dp = 400.dp,
    modifier: Modifier = Modifier
) {
    // Find which items are selected in this category
    val selectedItemIds = currentFilterPath
        .filter { it.categoryId == categoryId }
        .map { it.entityId }
        .toSet()

    // Create a scroll state
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Find the first selected item to scroll to
    val firstSelectedItem = remember(selectedItemIds, items) {
        items.firstOrNull { item ->
            when (item) {
                is com.example.jazzlibraryktroomjpcompose.domain.models.Instrument ->
                    selectedItemIds.contains(item.id)
                is com.example.jazzlibraryktroomjpcompose.domain.models.Artist ->
                    selectedItemIds.contains(item.id)
                is com.example.jazzlibraryktroomjpcompose.domain.models.Duration ->
                    selectedItemIds.contains(item.id)
                is com.example.jazzlibraryktroomjpcompose.domain.models.Type ->
                    selectedItemIds.contains(item.id)
                else -> false
            }
        }
    }

    // Auto-scroll when selection changes
    LaunchedEffect(firstSelectedItem, scrollState) {
        if (firstSelectedItem != null) {
            // We need to wait for layout to complete
            //delay(50) // Small delay to ensure layout is complete

            // We'll need a different approach since we can't get chip positions easily
            // Instead, let's implement a scroll-to-selected logic
        }
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = Dimens.commonPadding)
        )

        Spacer(modifier = Modifier.height(Dimens.smallSpacing))

        // Enhanced FlowLayout with automatic scroll
        EnhancedFlowLayout(
            items = items,
            selectedItemIds = selectedItemIds,
            onChipSelected = { categoryId, entityId, entityName, isSelected ->
                onChipSelected(categoryId, entityId, entityName, isSelected)
            },
            categoryId = categoryId,
            maxHeight = maxHeight,
            scrollState = scrollState
        )
    }
}

@Composable
private fun EnhancedFlowLayout(
    items: List<Any>,
    selectedItemIds: Set<Int>,
    onChipSelected: (Int, Int, String, Boolean) -> Unit,
    categoryId: Int,
    maxHeight: Dp,
    scrollState: ScrollState
) {
    // Track the Y position of selected chips
    var selectedChipY by remember { mutableFloatStateOf(0f) }
    var hasScrolled by remember { mutableStateOf(false) }

    // Track when we should scroll
    var shouldScrollToSelection by remember { mutableStateOf(false) }

    // Reset scroll flag when selection changes
    LaunchedEffect(selectedItemIds) {
        hasScrolled = false
        shouldScrollToSelection = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens.commonPadding)
    ) {
        Layout(
            content = {
                items.forEach { item ->
                    val chip = when (item) {
                        is com.example.jazzlibraryktroomjpcompose.domain.models.Instrument -> {
                            val isSelected = selectedItemIds.contains(item.id)
                            ChipContent(
                                text = item.name,
                                isSelected = isSelected,
                                onClick = { onChipSelected(categoryId, item.id, item.name, !isSelected) },
                                itemId = item.id,
                                videoCount = item.videoCount, // Pass videoCount
                                onPositionMeasured = { yPos, isSelectedChip ->
                                    if (isSelectedChip && !hasScrolled) {
                                        selectedChipY = yPos
                                        shouldScrollToSelection = true
                                    }
                                }
                            )
                        }
                        is com.example.jazzlibraryktroomjpcompose.domain.models.Artist -> {
                            val isSelected = selectedItemIds.contains(item.id)
                            ChipContent(
                                text = item.fullName,
                                isSelected = isSelected,
                                onClick = { onChipSelected(categoryId, item.id, item.fullName, !isSelected) },
                                itemId = item.id,
                                videoCount = item.videoCount, // Pass videoCount
                                onPositionMeasured = { yPos, isSelectedChip ->
                                    if (isSelectedChip && !hasScrolled) {
                                        selectedChipY = yPos
                                        shouldScrollToSelection = true
                                    }
                                }
                            )
                        }
                        is com.example.jazzlibraryktroomjpcompose.domain.models.Duration -> {
                            val isSelected = selectedItemIds.contains(item.id)
                            ChipContent(
                                text = item.name,
                                isSelected = isSelected,
                                onClick = { onChipSelected(categoryId, item.id, item.name, !isSelected) },
                                itemId = item.id,
                                videoCount = item.videoCount, // Pass videoCount
                                onPositionMeasured = { yPos, isSelectedChip ->
                                    if (isSelectedChip && !hasScrolled) {
                                        selectedChipY = yPos
                                        shouldScrollToSelection = true
                                    }
                                }
                            )
                        }
                        is com.example.jazzlibraryktroomjpcompose.domain.models.Type -> {
                            val isSelected = selectedItemIds.contains(item.id)
                            ChipContent(
                                text = item.name,
                                isSelected = isSelected,
                                onClick = { onChipSelected(categoryId, item.id, item.name, !isSelected) },
                                itemId = item.id,
                                videoCount = item.videoCount, // Pass videoCount
                                onPositionMeasured = { yPos, isSelectedChip ->
                                    if (isSelectedChip && !hasScrolled) {
                                        selectedChipY = yPos
                                        shouldScrollToSelection = true
                                    }
                                }
                            )
                        }
                        else -> {
                            // Fallback
                            Box(
                                modifier = Modifier
                                    .height(Dimens.pathChipHeight)
                                    .wrapContentWidth()
                                    .clip(RoundedCornerShape(Dimens.chipRoundedCorner))
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Unknown",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(Dimens.smallSpacing)
                                )
                            }
                        }
                    }

                    // Add the chip to the layout
                    Box(modifier = Modifier.wrapContentSize()) {
                        chip
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { measurables, constraints ->
            val horizontalSpacingPx = Dimens.smallSpacing.roundToPx()
            val verticalSpacingPx = Dimens.smallSpacing.roundToPx()

            var currentRow = 0
            var currentX = 0
            var currentY = 0
            var maxHeightInRow = 0

            // Measure all children first
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }

            // Calculate positions
            val positions = mutableListOf<Pair<Int, Int>>()

            placeables.forEach { placeable ->
                val width = placeable.width
                val height = placeable.height

                // Check if the item fits in current row
                if (currentX + width > constraints.maxWidth) {
                    // Move to next row
                    currentRow++
                    currentX = 0
                    currentY += maxHeightInRow + verticalSpacingPx
                    maxHeightInRow = 0
                }

                positions.add(Pair(currentX, currentY))

                currentX += width + horizontalSpacingPx
                maxHeightInRow = maxOf(maxHeightInRow, height)
            }

            val totalHeight = if (placeables.isNotEmpty()) {
                currentY + maxHeightInRow
            } else {
                0
            }

            layout(
                width = constraints.maxWidth,
                height = totalHeight
            ) {
                positions.forEachIndexed { index, (x, y) ->
                    placeables[index].placeRelative(x, y)
                }
            }
        }
    }

    // Auto-scroll after layout
    LaunchedEffect(shouldScrollToSelection, scrollState) {
        if (shouldScrollToSelection && !hasScrolled) {
            // Small delay to ensure layout is complete
            //delay(50) //100

            // Scroll to show the selected chip
            scrollState.animateScrollTo(
                value = selectedChipY.toInt() - 100, // Scroll a bit above the chip
                animationSpec = tween(durationMillis = 300)
            )

            hasScrolled = true
            shouldScrollToSelection = false
        }
    }
}

@Composable
private fun ChipContent(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    itemId: Int,
    videoCount: Int = 0,
    onPositionMeasured: (Float, Boolean) -> Unit
) {
    if (videoCount <= 0) return

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val borderWidth = if (isSelected) 1.dp else 1.dp

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .onGloballyPositioned { coordinates ->
                onPositionMeasured(coordinates.positionInParent().y, isSelected)
            }
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(Dimens.chipRoundedCorner))
                .background(backgroundColor)
                .clickable { onClick() }
                .border(
                    BorderStroke(borderWidth, borderColor),
                    RoundedCornerShape(Dimens.chipRoundedCorner)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(
                    horizontal = Dimens.chiptextHorizontalPadding,
                    vertical = Dimens.chiptextVerticalPadding
                )
            ) {
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

//                // DEBUG: Show the actual video count for debugging
//                Text(
//                    text = "($videoCount)",
//                    color = textColor.copy(alpha = 0.7f),
//                    style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
            }
        }
    }
}

@Composable
private fun PaginatedArtistChip(
    artist: com.example.jazzlibraryktroomjpcompose.domain.models.Artist,
    isSelected: Boolean,
    onPositionMeasured: (Float, Boolean) -> Unit,
    onClick: () -> Unit
) {
    if (artist.videoCount <= 0) return

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(Dimens.chipRoundedCorner))
            .background(backgroundColor)
            .clickable { onClick() }
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(Dimens.chipRoundedCorner)
            )
            .onGloballyPositioned { coordinates ->
                // Measure position and pass it up
                onPositionMeasured(coordinates.positionInParent().y, isSelected)
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = Dimens.chiptextHorizontalPadding,
                vertical = Dimens.chiptextVerticalPadding
            )
        ) {
            Text(
                text = artist.fullName,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

//            // Show video count if available
//            if (artist.videoCount >= 0) {
//                Text(
//                    text = "(${artist.videoCount})",
//                    color = textColor.copy(alpha = 0.7f),
//                    style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
//            }
        }
    }
}