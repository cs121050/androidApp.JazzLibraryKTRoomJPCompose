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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlin.math.roundToInt
import android.os.Build
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jazzlibraryktroomjpcompose.R
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerUiState
import com.example.jazzlibraryktroomjpcompose.ui.main.player.SmartYoutubePlayerHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.ui.main.util.generateIdenticon
import androidx.compose.runtime.key
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.window.Dialog
import org.json.JSONObject
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.DialogProperties
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.platform.LocalView
import kotlin.math.abs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.unit.Dp

class ScrollLockState {
    var isLocked by mutableStateOf(false)
}

val LocalScrollLock = compositionLocalOf { ScrollLockState() }

@OptIn(ExperimentalMaterial3Api::class)
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

    val leftDrawerOffset by animateDpAsState(
        targetValue = if (leftDrawerState == DrawerState.OPEN) 0.dp else (-320).dp
    )

    val bottomSheetState by viewModel.bottomSheetState.collectAsState()
    val context = LocalContext.current
    var backPressTime by remember { mutableLongStateOf(0L) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val playerUiState by playerViewModel.uiState.collectAsState()
    var activeCardRelativePosition by remember { mutableStateOf<IntOffset?>(null) }
    var activeCardSize by remember { mutableStateOf<IntSize?>(null) }
    var contentBoxRootPosition by remember { mutableStateOf(IntOffset.Zero) }



    //i have made the isPlayerVisible global (placed it in the viewmodel) so to access it independently
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()

    val cardUiStates by viewModel.cardUiStates.collectAsState()
    //witch tab is the main tab
    val currentTab by viewModel.currentTab.collectAsState()
    val hasArtistFilter = filterState.currentFilterPath.any { it.categoryId == 2 }
    val artistCount = if (hasArtistFilter) 1 else uiState.availableArtists.size
    val historyCount = 0 // Placeholder – you can later replace with a real count

    val scrollLockState = remember { ScrollLockState() }   // That is for the singleartistvie's wikidatacard scrolling, it locks the scrolling in order for items to consume the whole scrolling gesture

    //orientation detection
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFullscreen = isLandscape && playerUiState.isVisible

    var miniPlayerHeight by remember { mutableStateOf(0.dp) }

    // --- System UI control for fullscreen ---
    val systemUiController = rememberSystemUiController()
    var showBars by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val topTapThresholdPx = remember { with(density) { 80.dp.toPx() } }

    val view = LocalView.current

    val navBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current).dp

    val listState = rememberLazyListState()

    //DEBUGLOG
    LaunchedEffect(isFullscreen) {
        Log.d("Fullscreen", "isFullscreen: $isFullscreen, isLandscape: $isLandscape, playerVisible: ${playerUiState.isVisible}")
    }

    // When in Artist tab, force the player to mini mode if it's visible
    LaunchedEffect(currentTab, playerUiState.isVisible, playerUiState.isInMiniMode) {
        if (currentTab == MainTab.ARTISTS && playerUiState.isVisible && !playerUiState.isInMiniMode) {
            playerViewModel.minimizePlayer()
        }
    }

    // Auto‑hide bars after 3 seconds when they become visible
    LaunchedEffect(showBars, isFullscreen) {
        if (isFullscreen && showBars) {
            delay(3000)
            showBars = false
        }
    }

    // Apply system UI visibility and colors based on fullscreen state
    // Then replace the system UI control LaunchedEffect with this:
    LaunchedEffect(isFullscreen, showBars) {
        Log.d("Fullscreen", "System UI effect: isFullscreen=$isFullscreen, showBars=$showBars")
        if (isFullscreen) {
            // Immersive sticky flags prevent swipe from revealing bars
            view.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            // Control visibility via Accompanist (only shows bars when showBars=true)
            systemUiController.isStatusBarVisible = showBars
            systemUiController.isNavigationBarVisible = showBars
        } else {
            // Restore normal UI
            systemUiController.isStatusBarVisible = true
            systemUiController.isNavigationBarVisible = true
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

// Auto‑hide timer (unchanged)
    LaunchedEffect(showBars, isFullscreen) {
        if (isFullscreen && showBars) {
            Log.d("Fullscreen", "Bars visible, starting auto-hide timer")
            delay(3000)
            Log.d("Fullscreen", "Auto-hiding bars")
            showBars = false
        }
    }


    // Monitor tab changes and minimize player when leaving Videos tab
    LaunchedEffect(currentTab) {
        if (playerUiState.isVisible && !playerUiState.isInMiniMode) {
            playerViewModel.minimizePlayer()
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

            val nestedScrollConnection = remember(scrollLockState) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        // If the wiki card is locked, do NOT move the toolbar
                        if (scrollLockState.isLocked) {
                            return Offset.Zero
                        }
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

            // Update player playlist when the video list changes
            LaunchedEffect(videosToShow) {
                playerViewModel.updatePlaylist(videosToShow)
            }

            // Keep the player's current index in sync (e.g., after a load via card tap)
            LaunchedEffect(playerUiState.currentVideoId) {
                val videoId = playerUiState.currentVideoId
                if (videoId != null) {
                    val index = videosToShow.indexOfFirst { extractYouTubeVideoId(it.path) == videoId }
                    if (index != -1 && playerUiState.currentIndex != index) {
                        playerViewModel.updateCurrentIndex(index)
                    }
                }
            }

            // ----- CHIPS ROW (fixed) -----
            if (!isFullscreen) {
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
            }




            // LOCK all the scrolling gestures to ensure that a nested item consume all of it
            CompositionLocalProvider(LocalScrollLock provides scrollLockState) {



                // ----- PULL TO REFRESH + CONTENT -----

                //for hiding the top bar when on fullscreen
                val topPadding = if (isFullscreen) {
                    0.dp
                } else {
                    with(LocalDensity.current) { chipsHeightPx.intValue.toDp() }
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        viewModel.shuffleVideoList()
                        viewModel.shuffleArtists()
                        //todo// album list

                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding)
                ) {
                    // This Box contains toolbar, list, and player
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                            .onGloballyPositioned { coordinates ->
                                Log.d("Fullscreen", "Parent box size: ${coordinates.size}")
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
                                artistCount = artistCount,
                                historyCount = historyCount,
                                currentTab = currentTab,
                                onTabSelected = { viewModel.setCurrentTab(it) },
                                isPlayerVisible = isPlayerVisible,
                                onPrevious = { playerViewModel.previousVideo() },
                                onNext = { playerViewModel.nextVideo() },
                                onClose = { playerViewModel.closePlayer()},
                                playerViewModel = playerViewModel,
                                videos = videosToShow,
                                listState = listState,
                                currentFilterPath = filterState.currentFilterPath,
                                onTogglePlayerVisibility = { viewModel.togglePlayerVisibility() }
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
                            if (!isFullscreen) {
                                //depending of witch tab of the toolbarbox is selected give me the relevant screen
                                when (currentTab) {
                                    MainTab.VIDEOS -> VideoListContent(
                                        uiState = uiState,
                                        filterState = filterState,
                                        videosToShow = videosToShow,
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
                                        listState = listState,
                                        isPlayerVisible = isPlayerVisible,
                                        cardUiStates = cardUiStates,
                                        onCardTitleClick = { videoId ->
                                            viewModel.onCardTitleClick(
                                                videoId
                                            )
                                        }
                                    )

                                    MainTab.ARTISTS -> ArtistContent(
                                        modifier = Modifier.fillMaxSize(),
                                        artistsShuffled = uiState.availableArtistsDisplay,
                                        artistsBase = uiState.availableArtists,
                                        filterPath = filterState.currentFilterPath, // pass filter path
                                        onRefresh = { viewModel.shuffleArtists() },
                                        onArtistSelected = { artist ->
                                            viewModel.handleChipSelection(
                                                FilterPath.CATEGORY_ARTIST,
                                                artist.id,
                                                artist.fullName,
                                                true // add filter
                                            )
                                        }
                                    )

                                    MainTab.HISTORY -> HistoryContent(
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        // ----- PLAYER (draggable mini player) -----
                        if (playerUiState.isVisible) {
                            val density = LocalDensity.current
                            val scope = rememberCoroutineScope()
                            val configuration = LocalConfiguration.current
                            val context = LocalContext.current

                            // Dragging state (only used in mini mode)
                            val dragOffsetX = remember { Animatable(0f) }
                            val dragOffsetY = remember { Animatable(0f) }
                            val closeThresholdPx = with(density) { 100.dp.toPx() }
                            val xThresholdPx = with(density) { 100.dp.toPx() }

                            // Player size for boundary calculations
                            var playerSize by remember { mutableStateOf(IntSize.Zero) }
                            val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                            val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                            val marginPx = with(density) { 6.dp.toPx() }

                            // Reset offset when entering mini mode
                            LaunchedEffect(playerUiState.isInMiniMode) {
                                if (playerUiState.isInMiniMode) {
                                    dragOffsetY.snapTo(0f)
                                    dragOffsetY.snapTo(0f)
                                }
                            }

                            // Compute max offsets based on actual player size
                            val maxLeftOffset = remember(playerSize, screenWidthPx) {
                                if (playerSize.width > 0) {
                                    -(screenWidthPx - playerSize.width - marginPx)
                                } else -Float.MAX_VALUE
                            }
                            val maxUpOffset = remember(playerSize, screenHeightPx) {
                                if (playerSize.height > 0) {
                                    -(screenHeightPx - playerSize.height - marginPx)
                                } else -Float.MAX_VALUE
                            }

                            // --- Compute modifier based on fullscreen, mini, or attached ---
                            val playerModifier = when {
                                isFullscreen -> Modifier
                                    .fillMaxSize()
                                    .zIndex(10f)
                                    .graphicsLayer { clip = true }
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            if (offset.y < topTapThresholdPx) {
                                                Log.d("Fullscreen", "Top edge tap detected, showing bars")
                                                showBars = true
                                            }
                                        }
                                    }

                                playerUiState.isInMiniMode -> Modifier
                                    .size(width = 235.dp, height = 200.dp)  // IMPORTANT : change this to 205 to 205 to comply with youtube rules!
                                    .padding(bottom = 6.dp, end = 6.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset { IntOffset(dragOffsetX.value.roundToInt(), dragOffsetY.value.roundToInt()) }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                scope.launch {
                                                    dragOffsetX.stop()
                                                    dragOffsetY.stop()
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                scope.launch {
                                                    var newX = (dragOffsetX.value + dragAmount.x).coerceIn(maxLeftOffset, 0f)
                                                    var newY = (dragOffsetY.value + dragAmount.y).coerceIn(maxUpOffset, 0f)
                                                    dragOffsetX.snapTo(newX)
                                                    dragOffsetY.snapTo(newY)
                                                }
                                            },
                                            onDragEnd = {
                                                scope.launch {
                                                    val dx = dragOffsetX.value
                                                    val dy = dragOffsetY.value

                                                    // Horizontal drag exceeds threshold AND is dominant direction
                                                    if (abs(dx) > xThresholdPx && abs(dx) > abs(dy)) {
                                                        Toast.makeText(context, "TODO: return feature triggered", Toast.LENGTH_SHORT).show()
                                                        // Animate back
                                                        dragOffsetX.animateTo(0f)
                                                        dragOffsetY.animateTo(0f)
                                                    }
                                                    // Vertical drag exceeds close threshold
                                                    else if (abs(dy) > closeThresholdPx) {
                                                        playerViewModel.closePlayer()
                                                    }
                                                    // Otherwise snap back
                                                    else {
                                                        dragOffsetX.animateTo(0f)
                                                        dragOffsetY.animateTo(0f)
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch {
                                                    dragOffsetX.animateTo(0f)
                                                    dragOffsetY.animateTo(0f)
                                                }
                                            }
                                        )
                                    }
                                    .zIndex(5f)

                                else -> {
                                    // Attached to active card
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
                                                .zIndex(5f)
                                        }
                                    } ?: Modifier.size(0.dp)
                                }
                            }

                            //miniplayer's box
                            Box(
                                modifier = playerModifier
                                    .onGloballyPositioned { coordinates ->
                                        miniPlayerHeight = with(density) { coordinates.size.height.toDp() }
                                        Log.d("Fullscreen", "Player box size: ${coordinates.size}")
                                    }
                            ) {
                                SmartYoutubePlayerHost(
                                    key = playerUiState.playerInstanceId,
                                    videoId = playerUiState.currentVideoId,
                                    isFullscreen = isFullscreen,
                                    isMiniMode = playerUiState.isInMiniMode && !isFullscreen,
                                    onPlayerReady = { player -> playerViewModel.setPlayer(player) },
                                    onWebViewReady = { webView ->
                                        webView.post {
                                            // Remove any extra space
                                            webView.setPadding(0, 0, 0, 0)
                                            // Disable scrolling inside the WebView
                                            webView.isScrollContainer = false
                                            webView.isVerticalScrollBarEnabled = false
                                            webView.isHorizontalScrollBarEnabled = false
                                            webView.setInitialScale(100)
                                            // Force match parent
                                            webView.layoutParams = webView.layoutParams.apply { // FIX: eliminates the scroling of the youtube content inside the youtubelpayer after fullscreen
                                                width = ViewGroup.LayoutParams.MATCH_PARENT
                                                height = ViewGroup.LayoutParams.MATCH_PARENT
                                            }
                                            webView.requestLayout()
                                        }
                                    },
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

            // youtube Player controls overlay (added)
            PlayerControlsOverlay(
                isVisible = playerUiState.isVisible,
                isFullscreen = isFullscreen,
                isMiniMode = playerUiState.isInMiniMode,
                miniPlayerHeight = miniPlayerHeight,
                onPrevious = { playerViewModel.previousVideo() },
                onNext = { playerViewModel.nextVideo() },
                onClose = { playerViewModel.closePlayer() },
                modifier = Modifier.fillMaxSize()
            )
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

@Composable
fun toolbarBox(
    onFilterClick: () -> Unit,
    videoCount: Int,
    artistCount: Int,
    historyCount: Int,
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    isPlayerVisible: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    playerViewModel: PlayerViewModel,
    videos: List<Video>,
    currentFilterPath: List<FilterPath>,
    listState: LazyListState,
    onTogglePlayerVisibility: () -> Unit
) {
    SearchBar(
        onFilterClick = onFilterClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
    //Spacer(modifier = Modifier.height(16.dp))
    VideoStatsRow(
        videoCount = videoCount,
        artistCount = artistCount,
        historyCount = historyCount,
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        isPlayerVisible = isPlayerVisible,
        onTogglePlayerVisibility = onTogglePlayerVisibility,
        onNext = onNext,
        onClose = onClose,
        onPrevious = onPrevious,
        playerViewModel = playerViewModel,
        videos = videos,
        listState = listState,
        currentFilterPath = currentFilterPath,
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
    artistCount: Int,
    historyCount: Int,
    currentTab: MainTab,                         // already there
    onTabSelected: (MainTab) -> Unit,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    playerViewModel: PlayerViewModel,
    videos: List<Video>,
    listState: LazyListState,
    currentFilterPath: List<FilterPath>,
    modifier: Modifier = Modifier
) {
    val playerUiState by playerViewModel.uiState.collectAsState()
    val isVideoPlaying = playerUiState.isVisible && playerUiState.currentVideoId != null
    val controlsAccessible = isPlayerVisible && isVideoPlaying

    val pagerState = rememberPagerState(
        initialPage = if (controlsAccessible) 0 else 1,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()




    // Keep pager on minimise page when controls become inaccessible
    LaunchedEffect(controlsAccessible) {
        if (!controlsAccessible && pagerState.currentPage != 1) {
            pagerState.animateScrollToPage(1)
        }
    }

    LaunchedEffect(isVideoPlaying) {
        if (controlsAccessible && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
        Log.d("VIDEOSTATROW", "controlsAccessible: $controlsAccessible")
    }

    fun isVideoIndexVisible(index: Int): Boolean {
        return if (index in 0 until videos.size) {
            val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
            index in visibleIndices
        } else false
    }

    val loadFirstVideo: () -> Unit = {
        if (videos.isNotEmpty()) {
            val firstVideo = videos.first()
            val videoId = extractYouTubeVideoId(firstVideo.path)
            if (videoId != null) {
                val startInMiniMode = when {
                    currentTab != MainTab.VIDEOS -> true
                    else -> !isVideoIndexVisible(0)   // first video's index is 0
                }
                playerViewModel.loadVideo(
                    videoId = videoId,
                    cardId = firstVideo.locationId,
                    currentFilterPath = currentFilterPath,
                    startInMiniMode = startInMiniMode
                )
            }
        }
    }

// In LaunchedEffect that triggers when pagerState.currentPage == 0
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0 && !playerUiState.isVisible) {
            loadFirstVideo()
        }
    }


    val onNextClick: () -> Unit = {
        val currentVideoId = playerUiState.currentVideoId
        val currentIndex = playerUiState.currentIndex ?: -1
        val targetIndex = currentIndex + 1

        // Determine if we should start in mini mode
        val startInMiniMode = when {
            currentTab != MainTab.VIDEOS -> true                    // non-video tabs always mini
            targetIndex !in 0 until videos.size -> false            // fallback (shouldn't happen)
            else -> !isVideoIndexVisible(targetIndex)               // mini if card not visible
        }

        if (targetIndex in 0 until videos.size) {
            val targetVideo = videos[targetIndex]
            val videoId = extractYouTubeVideoId(targetVideo.path)
            if (videoId != null) {
                playerViewModel.loadVideo(
                    videoId = videoId,
                    cardId = targetVideo.locationId,
                    currentFilterPath = currentFilterPath,
                    startInMiniMode = startInMiniMode
                )
            }
        } else {
            // No next video – you might want to wrap around or do nothing
        }
    }

    val onPreviousClick: () -> Unit = {
        val currentIndex = playerUiState.currentIndex ?: -1
        val targetIndex = currentIndex - 1

        val startInMiniMode = when {
            currentTab != MainTab.VIDEOS -> true
            targetIndex !in 0 until videos.size -> false
            else -> !isVideoIndexVisible(targetIndex)
        }

        if (targetIndex in 0 until videos.size) {
            val targetVideo = videos[targetIndex]
            val videoId = extractYouTubeVideoId(targetVideo.path)
            if (videoId != null) {
                playerViewModel.loadVideo(
                    videoId = videoId,
                    cardId = targetVideo.locationId,
                    currentFilterPath = currentFilterPath,
                    startInMiniMode = startInMiniMode
                )
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab row (unchanged)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabText(
                text = "Videos ($videoCount)",
                selected = currentTab == MainTab.VIDEOS,
                onClick = { onTabSelected(MainTab.VIDEOS) }
            )
            TabText(
                text = "Artists ($artistCount)",
                selected = currentTab == MainTab.ARTISTS,
                onClick = { onTabSelected(MainTab.ARTISTS) }
            )
            TabText(
                text = "History",
                selected = currentTab == MainTab.HISTORY,
                onClick = { onTabSelected(MainTab.HISTORY) }
            )
        }

        // Pager area (controls / minimise)
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier.wrapContentWidth()
        ) { page ->
            when (page) {
                0 -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(onClick = onPreviousClick) {   // use custom previous
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }
                        IconButton(onClick = onNextClick) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                onClose()
                                pagerState.animateScrollToPage(1)
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                1 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconToggleButton(
                            checked = isPlayerVisible,
                            onCheckedChange = { onTogglePlayerVisibility() }
                        ) {
                            Icon(
                                imageVector = if (isPlayerVisible) Icons.Default.ViewList else Icons.Default.ViewModule,
                                contentDescription = if (isPlayerVisible) "Hide players" else "Show players",
                                tint = if (isPlayerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
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
    modifier: Modifier = Modifier,
    cardUiStates: Map<String, CardUiState>,
    onCardTitleClick: (String) -> Unit
) {
// Find the index of the currently active video card
    val activeCardIndex = videosToShow.indexOfFirst { it.locationId == playerUiState.activeCardId }
    val context = LocalContext.current
    val imageLoader = coil.ImageLoader(context)

    //helps with loading the thubnails faster while scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisible ->
                // Preload the next 5 items ahead
                val preloadRange = (firstVisible + 1)..(firstVisible + 5).coerceAtMost(videosToShow.lastIndex)

                for (index in preloadRange) {
                    val video = videosToShow.getOrNull(index) ?: continue
                    val thumbnailUrl = video.getThumbnailUrl() ?: continue

                    // Launch a background coroutine to fetch and cache the image
                    launch(Dispatchers.IO) {
                        val request = ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .build()
                        imageLoader.execute(request) // downloads and caches
                    }
                }
            }
    }
// Monitor scroll state and manage player mode based on card visibility
// that monitors scroll position and active card visibility.
    LaunchedEffect(
        listState.firstVisibleItemIndex,
        listState.layoutInfo,
        activeCardIndex,
        playerUiState.isVisible,
        playerUiState.isInMiniMode,
        isPlayerVisible
    ) {
        if (activeCardIndex >= 0 && playerUiState.isVisible) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isVisible = visibleItems.any { it.index == activeCardIndex }

            when {
                // If "Hide players" is on, force mini mode
                !isPlayerVisible && !playerUiState.isInMiniMode -> {
                    playerViewModel.minimizePlayer()
                }
                // If "Show players" is on, use scroll-based logic
                isPlayerVisible -> {
                    when {
                        isVisible && playerUiState.isInMiniMode -> playerViewModel.restoreFullMode()
                        !isVisible && !playerUiState.isInMiniMode -> playerViewModel.minimizePlayer()
                    }
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
                        },
                        cardState = cardUiStates[video.locationId] ?: CardUiState(),
                        onCardTitleClick = { onCardTitleClick(video.locationId) }
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
    modifier: Modifier = Modifier,
    cardState: CardUiState,
    onCardTitleClick: () -> Unit
) {
    val context = LocalContext.current


// Get the thumbnail URL from the video object
    val videoId = remember(video.path) { extractYouTubeVideoId(video.path) }
    val thumbnailUrl = video.getThumbnailUrl() // now defined here
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
                    .clickable { onCardTitleClick() },
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

            // --- Placeholder / thumbnail area (always visible) / Video area ---
            if (isPlayerVisible || cardState.showVideo) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = thumbnailUrl != null) { onCardClicked() }   // start video only on placeholder tap
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
                    // Placeholder content (e.g., play icon, future thumbnail)
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(42.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

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


            }

            // --- Expanded content (if any) ---
            if (cardState.expanded && isPlayerVisible) {
                // You can add more details here (description, artist list, etc.)
                Text(
                    text = "Additional info here...",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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

@Composable
fun ArtistContent(
    modifier: Modifier = Modifier,
    artistsShuffled: List<Artist>,
    artistsBase: List<Artist>,
    onRefresh: () -> Unit = {},
    filterPath: List<FilterPath>, // new parameter
    onArtistSelected: (Artist) -> Unit = {} // new callback
) {
    // Check if there's an artist filter
    val selectedArtist = filterPath
        .firstOrNull { it.categoryId == FilterPath.CATEGORY_ARTIST }
        ?.let { filter ->
            // Find the artist in the base list (or shuffled) by ID
            artistsBase.find { it.id == filter.entityId }
        }

    if (selectedArtist != null) {
        // Single artist view
        SingleArtistView(
            artist = selectedArtist,
            modifier = modifier
        )
        return
    }

    if (artistsShuffled.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No artists loaded")
        }
        return
    }

    // Single artist case
    if (artistsShuffled.size == 1) {
        SingleArtistView(
            artist = artistsShuffled.first(),
            modifier = modifier
        )
        return
    }

    // More than one artist: show grid pager (unchanged logic, but pass onArtistSelected to cards)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val cardWidth = (screenWidthDp - 32.dp - 8.dp) / 2
    val cardHeight = 80.dp

    var useAlphabetical by remember { mutableStateOf(false) }
    val artists = if (useAlphabetical) artistsBase else artistsShuffled
    val pages = remember(artists) { artists.chunked(8) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    LaunchedEffect(artistsShuffled) {
        useAlphabetical = false
        pagerState.scrollToPage(0)
    }

    var targetPage by remember { mutableIntStateOf(-1) }
    LaunchedEffect(useAlphabetical, targetPage) {
        if (useAlphabetical && targetPage >= 0 && targetPage < pages.size) {
            pagerState.scrollToPage(targetPage)
            targetPage = -1
        }
    }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val pageArtists = pages[page]
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(
                            modifier = Modifier.height(cardHeight * 4 + 8.dp * 3),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (row in 0 until 4) {
                                val start = row * 2
                                val rowArtists = pageArtists.slice(start until minOf(start + 2, pageArtists.size))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowArtists.forEach { artist ->
                                        key(artist.id) {
                                            ArtistCard(
                                                artist = artist,
                                                modifier = Modifier
                                                    .width(cardWidth)
                                                    .height(cardHeight),
                                                onClick = { onArtistSelected(artist) } // pass click handler
                                            )
                                        }
                                    }
                                    repeat(2 - rowArtists.size) {
                                        Spacer(modifier = Modifier.width(cardWidth))
                                    }
                                }
                            }
                        }
                    }
                }

                if (pages.size > 1) {
                    FastScrollingDotsRow(
                        pageCount = pages.size,
                        currentPage = pagerState.currentPage,
                        onSwitchToAlphabeticalAndScrollTo = { pageIndex ->
                            coroutineScope.launch {
                                if (!useAlphabetical) {
                                    useAlphabetical = true
                                    targetPage = pageIndex
                                } else {
                                    pagerState.scrollToPage(pageIndex)
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
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
                            val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
                            onSwitchToAlphabeticalAndScrollTo(pageIndex)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = change.position.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
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
                        val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
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

@Composable
fun ArtistCard(
    artist: Artist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val hasVideos = artist.videoCount > 0
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = shape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ArtistImage(
                artist = artist,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (artist.imageAuthor != null || artist.imageLicense != null) {
                    Text(
                        text = buildString {
                            artist.imageAuthor?.let { append(it) }
                            if (artist.imageLicense != null) {
                                if (artist.imageAuthor != null) append(" / ")
                                append(artist.imageLicense)
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Overlay for zero‑video artists
        if (!hasVideos) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("History Content")
    }
}

@Composable
fun ArtistImage(
    artist: Artist,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    // Generate on each composition – cheap enough
    val fallbackPainter = BitmapPainter(generateIdenticon(artist).asImageBitmap())

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artist.thumbnailUrl)
            .crossfade(true)
            .build(),
        contentDescription = artist.fullName,
        modifier = modifier,
        contentScale = contentScale,
        error = fallbackPainter,
    )
}

@Composable
fun SingleArtistView(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullscreenImage by remember { mutableStateOf(false) }

    // Determine if we have a real thumbnail
    val hasThumbnail = artist.thumbnailUrl != null
    val imageHeight = if (hasThumbnail) 300.dp else 150.dp

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Artist image (scrolls away)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clickable { showFullscreenImage = true }
            ) {
                if (hasThumbnail) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artist.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = artist.fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_error)
                    )
                } else {
                    // Identicon: centered, fixed size
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val fallbackPainter = BitmapPainter(generateIdenticon(artist).asImageBitmap())
                        Image(
                            painter = fallbackPainter,
                            contentDescription = artist.fullName,
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }

                // Attribution text (if any) anchored at top‑end
                if (artist.imageAuthor != null || artist.imageLicense != null) {
                    Text(
                        text = buildString {
                            artist.imageAuthor?.let { append(it) }
                            if (artist.imageLicense != null) {
                                if (artist.imageAuthor != null) append(" / ")
                                append(artist.imageLicense)
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }
        }

        // 2. Artist name
        item {
            Text(
                text = artist.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 3. Wiki info card
        item {
            WikiInfoCard(
                artist = artist,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Albums section header
        item {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 5. Horizontal albums row placeholder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Album cards will be placed here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Fullscreen image dialog (unchanged, scales to fit)
    if (showFullscreenImage) {
        Dialog(onDismissRequest = { showFullscreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // occupy all width and height
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullscreenImage = false }
            ) {
                if (hasThumbnail) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artist.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = artist.fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val fallbackPainter = BitmapPainter(generateIdenticon(artist).asImageBitmap())
                    Image(
                        painter = fallbackPainter,
                        contentDescription = artist.fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                IconButton(
                    onClick = { showFullscreenImage = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WikiInfoCard(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxHeight = screenHeight * 0.4f
    val minHeight = 150.dp

    // Provide the scroll lock state to children (like ScrollableTextPage)
    val scrollLockState = LocalScrollLock.current

    // Parse the stored wikipedia data
    val sections = remember(artist.wikipediaData) { parseWikipediaData(artist.wikipediaData) }
    val isLoading = sections.isEmpty() && artist.wikipediaData != null
    val error = if (artist.wikipediaData == null) "No Wikipedia data available" else null


    val allPages = remember(sections) {
        val contentPages = sections.map { (title, content) -> "### $title\n\n$content" }
        val attributionPage = """
            ℹ️ This information is sourced from Wikipedia.
            View the full article at: ${artist.wikipediaUrl ?: ""}
            
            This content is available under the Creative Commons Attribution-ShareAlike License.
        """.trimIndent()
        contentPages + listOf(attributionPage)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { allPages.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Nested scroll connection that consumes all leftover vertical scroll while the lock is active
    val nestedScrollConnection = remember(scrollLockState) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (scrollLockState.isLocked) {
                    // Consume any remaining vertical scroll so the parent never gets it
                    Offset(0f, available.y)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
            .padding(horizontal = 16.dp)
            .nestedScroll(nestedScrollConnection), // 👈 consume leftover scroll at card level
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // Provide the lock state to all children
        CompositionLocalProvider(LocalScrollLock provides scrollLockState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    allPages.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Wikipedia data found", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        // Pager area takes remaining space
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                ScrollableTextPage(
                                    text = allPages[page],
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        // Dots row below the pager
                        DotsRow(
                            pageCount = allPages.size,
                            currentPage = pagerState.currentPage,
                            onPageSelected = { pageIndex ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pageIndex)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ScrollableTextPage(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lines = text.lines()
    val scrollState = rememberScrollState()
    val scrollLockState = LocalScrollLock.current

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Lock the scroll while finger is down
                    scrollLockState.isLocked = true
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Release ||
                                event.type == PointerEventType.Exit) {
                                break
                            }
                        }
                    } finally {
                        scrollLockState.isLocked = false
                    }
                }
            }
    ) {
        for (line in lines) {
            if (line.startsWith("### ")) {
                // Render as heading
                Text(
                    text = line.removePrefix("### "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // Extract and handle URL if present
        val urlRegex = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+")
        val url = urlRegex.find(text)?.value
        if (url != null && text.contains(url)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🔗 $url",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            )
        }
    }
}

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
                            val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
                            onPageSelected(pageIndex)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (rowWidth > 0 && pageCount > 0) {
                            val x = change.position.x.coerceIn(0f, rowWidth.toFloat())
                            val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
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
                        val pageIndex = ((x / rowWidth) * pageCount).toInt().coerceIn(0, pageCount - 1)
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

data class WikipediaSummary(
    val title: String,
    val extract: String,
    val thumbnail: String? = null
)

fun cleanWikipediaText(rawText: String): String? {
    val lines = rawText.lines().toMutableList()

    // Remove everything up to and including the first "edit" line (main header)
    val firstEditIndex = lines.indexOfFirst { it.trim() == "edit" }
    if (firstEditIndex != -1) {
        lines.subList(0, firstEditIndex + 1).clear()
    }

    // Remove any subsequent "edit" line and everything after it
    val secondEditIndex = lines.indexOfFirst { it.trim() == "edit" }
    if (secondEditIndex != -1) {
        lines.subList(secondEditIndex, lines.size).clear()
    }

    val cleanedLines = mutableListOf<String>()
    for (line in lines) {
        var l = line.trim()
        if (l.isEmpty()) continue

        // Remove citations like [1], [2]
        l = l.replace(Regex("\\[.*?\\]"), "")

        // Skip lines that start with "obj"
        if (l.startsWith("obj", ignoreCase = true)) continue

        // Stop at footnote markers (lines starting with '^')
        if (l.startsWith('^')) break

        if (l.isNotBlank()) {
            cleanedLines.add(l)
        }
    }

    val result = cleanedLines.joinToString("\n")
    return if (result.length >= 40) result else null
}

private fun parseWikipediaData(jsonString: String?): List<Pair<String, String>> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
        val json = JSONObject(jsonString)
        val keys = json.keys()
        val list = mutableListOf<Pair<String, String>>()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.getString(key)
            list.add(key to value)
        }
        list
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@Composable
fun PlayerControlsOverlay(
    isVisible: Boolean,
    isFullscreen: Boolean,
    isMiniMode: Boolean,
    miniPlayerHeight: Dp,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val density = LocalDensity.current
    val navbarHeight = WindowInsets.navigationBars.getBottom(density).dp
    val hasNavBar = navbarHeight > 0.dp

    // Determine the bottom padding (for horizontal mode)
    val bottomPadding = when {
        isFullscreen -> 0.dp          // vertical mode uses no bottom padding
        isMiniMode && miniPlayerHeight > 0.dp -> miniPlayerHeight + 6.dp
        else -> if (hasNavBar) navbarHeight else 48.dp  // fallback when no navbar
    }

    // Icon color – use a light, semi‑transparent color to match system style
    val iconColor = Color.White.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)          // ensures it stays on top
    ) {
        if (isFullscreen) {
            // Vertical buttons on the right side, centered
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = iconColor)
                }
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = iconColor
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = iconColor)
                }
            }

        }
    }
}