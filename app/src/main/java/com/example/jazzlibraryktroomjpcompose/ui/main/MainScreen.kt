package com.example.jazzlibraryktroomjpcompose.ui.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlin.math.roundToInt
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerUiState
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.ui.main.player.MiniPlayerControls
import com.example.jazzlibraryktroomjpcompose.ui.main.player.YoutubePlayerHost
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.painterResource
import com.example.jazzlibraryktroomjpcompose.R

enum class MiniPlayerDragState { Bottom, Top, Hidden }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    // ... (all your existing state declarations remain unchanged)
    SetStatusBarColor(MaterialTheme.colorScheme.background)
    SetNavigationBarColor(MaterialTheme.colorScheme.background)

    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val leftDrawerState by viewModel.leftDrawerState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val showError by viewModel.showError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isPlayerVisible by remember { mutableStateOf(true) }

    val leftDrawerOffset by animateDpAsState(
        targetValue = if (leftDrawerState == DrawerState.OPEN) 0.dp else (-320).dp
    )

    val bottomSheetState by viewModel.bottomSheetState.collectAsState()
    val context = LocalContext.current
    var backPressTime by remember { mutableLongStateOf(0L) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val playerUiState by playerViewModel.uiState.collectAsState()
    var activeCardBounds by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var activeCardRelativePosition by remember { mutableStateOf<IntOffset?>(null) }
    var activeCardSize by remember { mutableStateOf<IntSize?>(null) }
    var contentBoxRootPosition by remember { mutableStateOf(IntOffset.Zero) }

    // --- NEW: swipeable state for mini player ---
    val swipeableState = rememberSwipeableState(initialValue = MiniPlayerDragState.Bottom)
    val containerSize = remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(playerUiState.isInMiniMode) {
        if (playerUiState.isInMiniMode) {
            activeCardBounds = null
            // Reset to bottom when entering mini mode
            swipeableState.snapTo(MiniPlayerDragState.Bottom)
        }
    }

    // --- When the player is hidden via swipe, close it ---
    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == MiniPlayerDragState.Hidden) {
            playerViewModel.closePlayer()
            // Reset for next time
            swipeableState.snapTo(MiniPlayerDragState.Bottom)
        }
    }

    // BackHandler (unchanged)
    BackHandler(
        enabled = true,
        onBack = {
            if (bottomSheetState != BottomSheetState.HIDDEN) {
                viewModel.setBottomSheetState(BottomSheetState.HIDDEN)
                return@BackHandler
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressTime > 500) {
                backPressTime = currentTime
            } else {
                (context as? android.app.Activity)?.finish()
            }
        }
    )

    if (loadingState == LoadingState.LOADING && uiState.videos.isEmpty()) {
        LoadingScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- Chips row measurement ---
            val chipsHeightPx = remember { mutableIntStateOf(0) }
            val toolbarHeightPx = remember { mutableIntStateOf(0) }
            val toolbarOffset = remember { mutableFloatStateOf(0f) }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        val delta = available.y
                        val newOffset = (toolbarOffset.floatValue + delta)
                            .coerceIn(-toolbarHeightPx.intValue.toFloat(), 0f)
                        val consumed = newOffset - toolbarOffset.floatValue
                        toolbarOffset.floatValue = newOffset
                        return Offset(0f, consumed)
                    }
                }
            }

            val videosToShow = if (filterState.currentFilterPath.isEmpty()) {
                uiState.videos
            } else {
                uiState.filteredVideos
            }

            val listState = rememberLazyListState()

            // ----- CHIPS ROW (fixed) -----
            ActiveFilterChipsRow(
                filterPath = filterState.currentFilterPath,
                onMenuClick = { viewModel.toggleLeftDrawer() },
                onChipClick = { categoryId, entityId, entityName ->
                    viewModel.handleChipSelection(categoryId, entityId, entityName, false)
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        chipsHeightPx.intValue = coordinates.size.height
                    }
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(7f)
            )

            // ----- PULL TO REFRESH + CONTENT -----
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.shuffleVideoList() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = with(LocalDensity.current) {
                            chipsHeightPx.intValue.toDp()
                        }
                    )
            ) {
                // This Box contains toolbar, list, and player
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .onSizeChanged { size ->
                            containerSize.value = size
                        }
                        .onGloballyPositioned { coordinates ->
                            contentBoxRootPosition = IntOffset(
                                x = coordinates.positionInRoot().x.roundToInt(),
                                y = coordinates.positionInRoot().y.roundToInt()
                            )
                        }
                ) {
                    // ----- TOOLBAR (unchanged) -----
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                toolbarHeightPx.intValue = coordinates.size.height
                            }
                            .offset {
                                IntOffset(0, toolbarOffset.floatValue.roundToInt())
                            }
                            .background(MaterialTheme.colorScheme.background)
                            .zIndex(6f)
                    ) {
                        toolbarBox(
                            onFilterClick = { viewModel.toggleBottomSheet() },
                            videoCount = videosToShow.size,
                            isPlayerVisible = isPlayerVisible,
                            onTogglePlayerVisibility = { isPlayerVisible = !isPlayerVisible }
                        )
                    }

                    // ----- VIDEO LIST (unchanged) -----
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = with(LocalDensity.current) {
                                    (toolbarHeightPx.intValue + toolbarOffset.floatValue)
                                        .coerceAtLeast(0f)
                                        .toDp()
                                }
                            )
                    ) {
                        VideoListContent(
                            uiState = uiState,
                            filterState = filterState,
                            videosToShow = videosToShow,
                            isPlayerVisible = isPlayerVisible,
                            onRefresh = { viewModel.safeRefreshDataFromAPI() },
                            onActiveCardBoundsChanged = { cardId, rootPosition, size ->
                                if (cardId == playerUiState.activeCardId) {
                                    val relativePos = rootPosition - contentBoxRootPosition
                                    activeCardRelativePosition = relativePos
                                    activeCardSize = size
                                }
                            },
                            playerUiState = playerUiState,
                            playerViewModel = playerViewModel,
                            listState = listState
                        )
                    }

                    // ----- PLAYER (draggable mini player) -----
                    val density = LocalDensity.current
                    if (playerUiState.isVisible) {
                        val baseModifier = if (playerUiState.isInMiniMode) {
                            val playerWidth = 240.dp
                            val playerHeight = 160.dp
                            val playerPadding = 16.dp

                            // Compute anchors only when container size is known
                            val anchors = if (containerSize.value.height > 0) {
                                val containerH = containerSize.value.height
                                val playerH = with(density) { playerHeight.roundToPx() }
                                val paddingPx = with(density) { playerPadding.roundToPx() }

                                // Bottom anchor: 0 offset from aligned bottom‑end position
                                val bottomY = 0f
                                // Top anchor: move up so top edge is at paddingPx from top
                                val topY = -(containerH - playerH - 2 * paddingPx).toFloat()
                                // Hidden anchor: off‑screen top
                                val hiddenY = -containerH.toFloat()

                                mapOf(
                                    bottomY to MiniPlayerDragState.Bottom,
                                    topY to MiniPlayerDragState.Top,
                                    hiddenY to MiniPlayerDragState.Hidden
                                )
                            } else {
                                // Fallback: simple anchors (just bottom)
                                mapOf(0f to MiniPlayerDragState.Bottom)
                            }

                            Modifier
                                .size(playerWidth, playerHeight)
                                .padding(playerPadding)
                                .align(Alignment.BottomEnd)
                                .swipeable(
                                    state = swipeableState,
                                    anchors = anchors,
                                    orientation = Orientation.Vertical,
                                    enabled = true,
                                    thresholds = { _, _ -> FractionalThreshold(0.3f) }
                                )
                                .offset {
                                    // Apply the vertical offset from swipeable state
                                    IntOffset(0, swipeableState.offset.value.roundToInt())
                                }
                        } else {
                            // Full mode: position over the active card
                            activeCardRelativePosition?.let { pos ->
                                activeCardSize?.let { size ->
                                    Modifier
                                        .size(
                                            width = with(density) { size.width.toDp() },
                                            height = with(density) { size.height.toDp() }
                                        )
                                        .graphicsLayer {
                                            translationX = pos.x.toFloat()
                                            translationY = pos.y.toFloat()
                                        }
                                }
                            } ?: Modifier.size(0.dp)
                        }

                        Box(
                            modifier = baseModifier
                                .zIndex(5f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            YoutubePlayerHost(
                                modifier = Modifier.fillMaxSize(),
                                onPlayerReady = { youTubePlayer ->
                                    coroutineScope.launch {
                                        playerViewModel.setPlayer(youTubePlayer)
                                    }
                                }
                            )

                            if (playerUiState.isInMiniMode) {
                                MiniPlayerControls(
                                    isPlaying = playerUiState.isPlaying,
                                    onLeftTap = { playerViewModel.onMiniPlayerLeftTap() },
                                    onCenterTap = { playerViewModel.onMiniPlayerCenterTap() },
                                    onRightTap = { playerViewModel.onMiniPlayerRightTap() },
                                    onClose = { playerViewModel.closePlayer() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // ----- LEFT DRAWER, BOTTOM SHEET, SNACKBAR (unchanged) -----
            LeftDrawer(
                isOpen = leftDrawerState == DrawerState.OPEN,
                onClose = { viewModel.toggleLeftDrawer() },
                onRefreshClick = { viewModel.safeRefreshDataFromAPI() },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .offset(x = leftDrawerOffset)
                    .zIndex(8f)
            )

            YouTubeLikeBottomSheet(
                viewModel = viewModel,
                uiState = uiState,
                filterState = filterState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(8f)
            )

            if (showError && errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.largePadding)
                        .zIndex(9f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("Dismiss")
                            }
                        },
                        modifier = Modifier.padding(Dimens.commonPadding)
                    ) {
                        Text(errorMessage!!)
                    }
                }
            }
        }
    }
}

// Updated LoadingScreen (simpler)
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(Dimens.largeSpacing))
            Text(
                text = "Loading Jazz Library...",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    uiState: MainUiState,
    filterState: FilterState,
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    playerUiState: PlayerUiState,
    onMenuClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit,
    isRefreshing: Boolean,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Heights of the chips row and toolbar (used for offsets and padding)
    val chipsHeightPx = remember { mutableIntStateOf(0) }
    val toolbarHeightPx = remember { mutableIntStateOf(0) }

    // Vertical offset of the toolbar (0 = fully visible, negative = hidden)
    val toolbarOffset = remember { mutableFloatStateOf(0f) }

    // Nested scroll connection to update toolbar offset based on list scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // Calculate new offset, clamped between -fullHeight and 0
                val newOffset = (toolbarOffset.floatValue + delta)
                    .coerceIn(-toolbarHeightPx.intValue.toFloat(), 0f)
                val consumed = newOffset - toolbarOffset.floatValue
                toolbarOffset.floatValue = newOffset
                // Consume the vertical scroll used to move the toolbar
                return Offset(0f, consumed)
            }
        }
    }

    val videosToShow = if (filterState.currentFilterPath.isEmpty()) {
        uiState.videos
    } else {
        uiState.filteredVideos
    }

    //for auto-scrolling to the top of videolistcontent after refresh
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Fixed chips row (always visible, highest z‑order) ---
        // Placed outside PullToRefreshBox so it stays on top of the refresh indicator.
        ActiveFilterChipsRow(
            filterPath = filterState.currentFilterPath,
            onMenuClick = onMenuClick,
            onChipClick = { categoryId, entityId, entityName ->
                viewModel.handleChipSelection(categoryId, entityId, entityName, false)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                // Measure its height to later offset the PullToRefreshBox
                .onGloballyPositioned { coordinates ->
                    chipsHeightPx.intValue = coordinates.size.height
                }
                .background(MaterialTheme.colorScheme.background)
                .zIndex(1f)  // Ensures chips row is drawn above everything else
        )

        // --- PullToRefreshBox, shifted down by the height of the chips row ---
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onShuffle,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(LocalDensity.current) {
                        chipsHeightPx.intValue.toDp()
                    }
                )
        ) {
            // --- Inner content: toolbar + video list with nested scroll ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Attach scroll connection to receive scroll events from the list
                    .nestedScroll(nestedScrollConnection)
            ) {
                // Video list container (drawn first, so it appears below the toolbar)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = with(LocalDensity.current) {
                                // Visible top space = toolbar height minus hidden part
                                (toolbarHeightPx.intValue + toolbarOffset.floatValue)
                                    .coerceAtLeast(0f)
                                    .toDp()
                            }
                        )
                ) {
                    VideoListContent(
                        uiState = uiState,
                        filterState = filterState,
                        videosToShow = videosToShow,
                        isPlayerVisible = isPlayerVisible,
                        onRefresh = onRefresh,
                        onActiveCardBoundsChanged = onActiveCardBoundsChanged,
                        playerUiState = playerUiState,
                        playerViewModel = playerViewModel,
                        listState = listState
                    )
                }

                // --- Toolbar column (scrolls away, drawn on top of the list) ---
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        // Measure its full height to know how far it can scroll
                        .onGloballyPositioned { coordinates ->
                            toolbarHeightPx.intValue = coordinates.size.height
                        }
                        // Apply vertical offset based on scroll (now without chips height)
                        .offset {
                            IntOffset(0, toolbarOffset.floatValue.roundToInt())
                        }
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    toolbarBox(
                        onFilterClick = onFilterClick,
                        videoCount = videosToShow.size,
                        isPlayerVisible = isPlayerVisible,
                        onTogglePlayerVisibility = onTogglePlayerVisibility
                    )
                }
            }
        }
    }
}

@Composable
fun toolbarBox(
    onFilterClick: () -> Unit,
    videoCount: Int,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit
) {
    SearchBar(
        onFilterClick = onFilterClick,
        modifier = Modifier.fillMaxWidth()
            .padding(top = 4.dp)
    )
    //Spacer(modifier = Modifier.height(16.dp))
    VideoStatsRow(
        videoCount = videoCount,
        isPlayerVisible = isPlayerVisible,
        onTogglePlayerVisibility = onTogglePlayerVisibility,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ActiveFilterChipsRow(
    filterPath: List<FilterPath>,  // Use your actual type here
    onMenuClick: () -> Unit,
    onChipClick: (categoryId: Int, entityId: Int, entityName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(bottom = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clickable Logo Text
        Text(
            text = "Jazzli",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { onMenuClick() }
                .padding(end = 16.dp)
                .align(Alignment.CenterVertically)
        )

        // Horizontally scrolling chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filterPath.isNotEmpty()) {
                filterPath.forEach { filter ->
                    FilterPathChip(
                        text = filter.entityName,
                        isSelected = false,  // always false – chips are not "selected", just showing applied filters
                        onClick = {
                            onChipClick(filter.categoryId, filter.entityId, filter.entityName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoStatsRow(
    videoCount: Int,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Videos ($videoCount)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        // IconToggleButton for global player visibility
        IconToggleButton(
            checked = isPlayerVisible,
            onCheckedChange = { onTogglePlayerVisibility() }
        ) {
            Icon(
                imageVector = if (isPlayerVisible)
                    Icons.Default.ViewList
                else
                    Icons.Default.ViewModule,
                contentDescription = if (isPlayerVisible)
                    "Hide players"
                else
                    "Show players",
                tint = if (isPlayerVisible)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onMenuClick: () -> Unit,
    onRefresh: () -> Unit,
    onFilterClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior, // Add this parameter
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "Jazzli",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
            }
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters")
            }
        },
        scrollBehavior = scrollBehavior, // Pass scroll behavior to TopAppBar
        modifier = modifier
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    videoCount: Int,
    onMenuClick: () -> Unit,
    onRefresh: () -> Unit,
    onFilterClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    // No nestedScroll here – scrollBehavior is already on TopAppBar
                    .padding(16.dp) // Consider using windowInsets if needed
            ) {
                SearchBar(
                    onFilterClick = onFilterClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                VideoStatsRow(
                    videoCount = videoCount,
                    isPlayerVisible = isPlayerVisible,
                    onTogglePlayerVisibility = onTogglePlayerVisibility,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier // outer modifier for TopAppBar itself
    )
}

@Composable
private fun SearchBar(
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = modifier,
        placeholder = { Text("Search videos, artists...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Open Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun VideoListContent(
    uiState: MainUiState,
    filterState: FilterState,
    videosToShow: List<Video>,
    isPlayerVisible: Boolean,
    playerUiState: PlayerUiState,
    onRefresh: () -> Unit,
    listState: LazyListState,
    playerViewModel: PlayerViewModel,
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
// Find the index of the currently active video card
    val activeCardIndex = videosToShow.indexOfFirst { it.locationId == playerUiState.activeCardId }



// Monitor scroll state and manage player mode based on card visibility
    LaunchedEffect(
        listState.firstVisibleItemIndex,
        listState.layoutInfo,
        activeCardIndex,
        playerUiState.isVisible,
        playerUiState.isInMiniMode
    ) {
        if (activeCardIndex >= 0 && playerUiState.isVisible) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isVisible = visibleItems.any { it.index == activeCardIndex }

            when {
                // Card is visible but player is mini → restore full mode
                isVisible && playerUiState.isInMiniMode -> {
                    playerViewModel.restoreFullMode()
                }
                // Card is invisible and player is full → minimize
                !isVisible && !playerUiState.isInMiniMode -> {
                    playerViewModel.minimizePlayer()
                }
            }
        }
    }

    // Effect to handle removal of the active card from the list
    LaunchedEffect(
        videosToShow,
        playerUiState.activeCardId,
        playerUiState.isVisible,
        playerUiState.isInMiniMode
    ) {
        if (playerUiState.isVisible && !playerUiState.isInMiniMode) {
            val activeCardExists = playerUiState.activeCardId != null &&
                    videosToShow.any { it.locationId == playerUiState.activeCardId }
            if (!activeCardExists) {
                playerViewModel.minimizePlayer()
            }
        }
    }

    if (filterState.isFiltering) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Applying filters...")
            }
        }
    } else {
        if (videosToShow.isEmpty()) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (uiState.videos.isEmpty())
                            "No videos in library"
                        else
                            "No videos found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.videos.isEmpty())
                            "Try refreshing data"
                        else
                            "Try changing your filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (uiState.videos.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Data")
                        }
                    }
                }
            }
        } else {
            LaunchedEffect(videosToShow) {
                listState.animateScrollToItem(0)
            }

            LazyColumn(
                state = listState,
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = videosToShow,
                    key = { it.id }
                ) { video ->
                    // 🔁 Pass only isPlayerVisible – no per‑card toggle callback
                    VideoCard(
                        video = video,
                        isPlayerVisible = isPlayerVisible,
                        isActive = video.locationId == playerUiState.activeCardId,
                        onActiveCardBoundsChanged = { cardId, position, size ->
                            onActiveCardBoundsChanged(cardId, position, size)
                        },
                        onCardClicked = {
                            val videoId = extractYouTubeVideoId(video.path)
                            if (videoId != null) {
                                playerViewModel.loadVideo(
                                    videoId = videoId,
                                    cardId = video.locationId,
                                    currentFilterPath = filterState.currentFilterPath
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCard(
    video: Video,
    isPlayerVisible: Boolean,
    isActive: Boolean, // true if this card is the currently active one
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    onCardClicked: () -> Unit, // called when user taps to load video
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    val videoId = remember(video.path) {
        extractYouTubeVideoId(video.path)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- Video info row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // --- Placeholder / thumbnail area (always visible) ---
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onCardClicked() }   // start video only on placeholder tap
                    .then(
                        if (isActive) {
                            Modifier.onPlaced { coordinates ->
                                onActiveCardBoundsChanged(
                                    video.locationId,
                                    IntOffset(
                                        x = coordinates.positionInRoot().x.roundToInt(),
                                        y = coordinates.positionInRoot().y.roundToInt()
                                    ),
                                    IntSize(coordinates.size.width, coordinates.size.height)
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                // Placeholder content (e.g., play icon, future thumbnail)
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    modifier = Modifier.align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Get the thumbnail URL from the video object
                val thumbnailUrl = video.getThumbnailUrl() // or video.getThumbnailUrl("maxresdefault")

                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Video thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_error)
                    )
                } else {
                    // Fallback when URL is invalid
                    Icon(
                        Icons.Default.BrokenImage,
                        contentDescription = "Invalid video",
                        modifier = Modifier.align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Optional: keep the fullscreen button (opens YouTube app)
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.path))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Open in YouTube app",
                        tint = Color.White
                    )
                }
            }

            // --- Expanded content (if any) ---
            if (expanded) {
                // You can add more details here (description, artist list, etc.)
                Text(
                    text = "Additional info here...",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
//            // --- YouTube Player (collapsible) ---
//            AnimatedVisibility(
//                visible = isPlayerVisible || expanded,
//                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)),
//                exit  = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300))
//            ) {
//                Column {
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.surfaceVariant)
//                    ) {
//                        if (videoId != null) {
//                            YoutubeVideoPlayer(
//                                videoId = videoId,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        } else {
//                            Box(
//                                modifier = Modifier.fillMaxSize(),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "Invalid video URL",
//                                    color = MaterialTheme.colorScheme.error
//                                )
//                            }
//                        }
//
//                        // --- Fullscreen button (opens YouTube app) ---
//                        IconButton(
//                            onClick = {
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.path))
//                                context.startActivity(intent)
//                            },
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
//                                .padding(8.dp)
//                                .size(48.dp)
//                                .background(
//                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
//                                    shape = RoundedCornerShape(24.dp)
//                                )
//                        ) {
//                            Icon(
//                                Icons.Default.Fullscreen,
//                                contentDescription = "Open in YouTube app",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//            }
        }
    }
}

private fun extractYouTubeVideoId(url: String): String? {
    val pattern = "(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([a-zA-Z0-9_-]{11})"
    val regex = Regex(pattern)
    return regex.find(url)?.groupValues?.get(1)
}

@Composable
fun FilterPathChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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

    // Use the same border width logic as ChipContent
    val borderWidth = if (isSelected) 1.dp else 1.dp

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
                vertical = 6.dp
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
        }
    }
}

@Composable
fun SetStatusBarColor(color: Color) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window

    DisposableEffect(window, color) {
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // setStatusBarColor is NOT deprecated – it's a stable API since Lollipop
            window.setStatusBarColor(color.toArgb())
        }
        onDispose { } // Required by DisposableEffect, but we don't need to restore
    }
}

@Composable
fun SetNavigationBarColor(color: Color) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window

    DisposableEffect(window, color) {
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color.toArgb())
        }
        onDispose { }
    }
}