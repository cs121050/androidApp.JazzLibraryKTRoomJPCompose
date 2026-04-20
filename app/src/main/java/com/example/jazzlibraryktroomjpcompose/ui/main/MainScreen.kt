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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.platform.LocalView
import kotlin.math.abs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.Dp
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import kotlinx.coroutines.flow.map

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jazzlibraryktroomjpcompose.ui.main.util.instrumentColor
import java.text.DecimalFormat

import androidx.compose.animation.AnimatedVisibility as CoreAnimatedVisibility

enum class AlbumGridTab { MAIN, FEATURED }
enum class SortDirection { ASC, DESC }

private const val TAG = "AlbumGridView"

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

    val currentFilterPathId by viewModel.currentFilterPathId.collectAsState()

    val currentMediaEntry by playerViewModel.currentFilterPathMedia.collectAsState()
    val currentMediaEntryTypeOfMedia = currentMediaEntry?.typeOfMedia

    val scrollToAlbumsTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(currentFilterPathId) {
        Log.d("MainScreen", "currentFilterPathId changed to: $currentFilterPathId")
    }

    //DEBUGLOG
    LaunchedEffect(isFullscreen) {
        Log.d("Fullscreen", "isFullscreen: $isFullscreen, isLandscape: $isLandscape, playerVisible: ${playerUiState.isVisible}")
    }

    LaunchedEffect(playerUiState.currentVideoId) {
        if (playerUiState.currentVideoId != null) {
            viewModel.loadEnrichedHistory()
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
            if (viewModel.hasPreviousHistory()) {
                viewModel.goBack()
                return@BackHandler
            }
            // Otherwise, handle app exit
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
                        viewModel.shuffleAlbums()

                        viewModel.refreshHistory()
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
                                onClose = { playerViewModel.closePlayer(currentFilterPathId)},
                                playerViewModel = playerViewModel,
                                videos = videosToShow,
                                listState = listState,
                                currentFilterPath = filterState.currentFilterPath,
                                currentFilterPathId = currentFilterPathId,
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
                                        currentFilterPathId = currentFilterPathId,
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
                                        albumsDisplay = uiState.availableAlbumsDisplay,
                                        filterPath = filterState.currentFilterPath, // pass filter path
                                        onRefresh = { viewModel.shuffleArtists() },
                                        onArtistSelected = { artist ->
                                            viewModel.handleChipSelection(
                                                FilterPath.CATEGORY_ARTIST,
                                                artist.id,
                                                artist.fullName,
                                                true // add filter
                                            )
                                        },
                                        filteredAlbums = uiState.filteredAlbums,
                                        currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
                                        currentFilterPathId = currentFilterPathId,
                                        minimiseMaximiseToggle = isPlayerVisible,
                                        onAlbumSelected = { album ->
                                            val alreadyFiltered = filterState.currentFilterPath.any {
                                                it.categoryId == FilterPath.CATEGORY_ARTIST && it.entityId == album.artistId
                                            }
                                            if (!alreadyFiltered && album.artistId != null) {
                                                viewModel.handleChipSelection(
                                                    FilterPath.CATEGORY_ARTIST,
                                                    album.artistId,
                                                    album.artistFullName ?: "Unknown Artist",
                                                    true
                                                )
                                            }
                                            // Always scroll to albums section after clicking an album card
                                            scrollToAlbumsTrigger.value++
                                        },
                                        scrollToAlbumsTrigger = scrollToAlbumsTrigger
                                    )

                                    MainTab.HISTORY -> HistoryContent(
                                        modifier = Modifier.fillMaxSize(),
                                        viewModel = viewModel,
                                        playerViewModel = playerViewModel,
                                        onRefresh = { viewModel.refreshHistory() },
                                        isPlayerVisible = isPlayerVisible
                                    )
                                }
                            }
                        }

                        // ----- PLAYER (draggable mini player) -----
                        if (playerUiState.isVisible) {
                            val density = LocalDensity.current
                            val configuration = LocalConfiguration.current
                            val context = LocalContext.current

                            // Dragging state (only used in mini mode)
                            val dragOffsetY = remember { Animatable(0f) }

                            // Player size for boundary calculations
                            var playerSize by remember { mutableStateOf(IntSize.Zero) }
                            val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                            val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                            val marginPx = with(density) { 6.dp.toPx() }

                            // Reset offset when entering mini mode
                            LaunchedEffect(playerUiState.isInMiniMode) {
                                if (playerUiState.isInMiniMode) {
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
                            Row(
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
                                    onPlayerReady = { player ->
                                        playerViewModel.setPlayer(
                                            player
                                        )
                                    },
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
                                            webView.layoutParams =
                                                webView.layoutParams.apply { // FIX: eliminates the scroling of the youtube content inside the youtubelpayer after fullscreen
                                                    width =
                                                        ViewGroup.LayoutParams.MATCH_PARENT
                                                    height =
                                                        ViewGroup.LayoutParams.MATCH_PARENT
                                                }
                                            webView.requestLayout()
                                        }
                                    },
                                    onVideoEnded = {
                                        playerViewModel.nextVideo(startInMiniMode = currentTab != MainTab.VIDEOS)
                                    },
                                    modifier = Modifier
                                        .weight(1f)          // Takes all available space after controls
                                        .fillMaxHeight()
                                )

                                if (isFullscreen ) {
                                    FullscreenControlsColumn(
                                        onShare = { /* TODO */ },
                                        onCast = { /* TODO */ },
                                        onBack = { /* TODO */ },
                                        onPrevious = { playerViewModel.previousVideo() },
                                        onNext = { playerViewModel.nextVideo() },
                                        onClose = { playerViewModel.closePlayer(currentFilterPathId) },
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.background)
                                            .zIndex(10f)
                                    )
                                }
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
                onClearHistoryClick = {viewModel.clearHistory() },
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
    currentFilterPathId: Int?,
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
        currentFilterPathId = currentFilterPathId,
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
    currentFilterPathId: Int?,
    modifier: Modifier = Modifier
) {
    val playerUiState by playerViewModel.uiState.collectAsState()
    val isVideoPlaying = playerUiState.isVisible && playerUiState.currentVideoId != null
    val controlsAccessible = isPlayerVisible && isVideoPlaying

    val pagerState = rememberPagerState(
        initialPage = if (controlsAccessible) 1 else 2,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Type of media: null by default, becomes 0 when (page 0 or 1) are visible
    var currentTypeOfMedia by remember { mutableStateOf<Int?>(null) }

    // Keep pager on minimise page when controls become inaccessible
    LaunchedEffect(controlsAccessible) {
        if (!controlsAccessible && pagerState.currentPage != 2) {
            pagerState.animateScrollToPage(2)
        }
    }

    LaunchedEffect(isVideoPlaying) {
        if (controlsAccessible && pagerState.currentPage != 1) {
            pagerState.animateScrollToPage(1)
        }
        Log.d("VIDEOSTATROW", "controlsAccessible: $controlsAccessible")
    }

    // Set currentTypeOfMedia based on which page is active
    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0, 1 -> currentTypeOfMedia = 0 // educational media controls
            3, 4 -> currentTypeOfMedia = 0 // album media controls
        }
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
                    startInMiniMode = startInMiniMode,
                    mediaDbId = firstVideo.id,
                    filterPathId = currentFilterPathId,
                    typeOfMedia = currentTypeOfMedia
                )
            }
        }
    }

// In LaunchedEffect that triggers when pagerState.currentPage == 0
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 && !playerUiState.isVisible) {
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
                    startInMiniMode = startInMiniMode,
                    mediaDbId = targetVideo.id,
                    filterPathId = currentFilterPathId,
                    typeOfMedia = currentTypeOfMedia
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
                    startInMiniMode = startInMiniMode,
                    mediaDbId = targetVideo.id,
                    filterPathId = currentFilterPathId,
                    typeOfMedia = currentTypeOfMedia
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
                    // Extra controls (share, cast, back)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(onClick = {
                            Toast.makeText(context, "Share - to be implemented", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Cast - to be implemented", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Cast, contentDescription = "Cast")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Back - to be implemented", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
                1 -> {
                    // Playback controls (previous, next, close)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(onClick = onPreviousClick) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }
                        IconButton(onClick = onNextClick) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                onClose()
                                pagerState.animateScrollToPage(2)
                                currentTypeOfMedia = null
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                2 -> {
                    // Global toggle (show/hide players)
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
    currentFilterPathId: Int?,
    onCardTitleClick: (String) -> Unit
) {
// Find the index of the currently active video card
    val activeCardIndex = videosToShow.indexOfFirst { it.locationId == playerUiState.activeCardId }
    val context = LocalContext.current
    val imageLoader = coil.ImageLoader(context)

    val typeOfMedia = 0 // educational videos only here.

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
                                    currentFilterPath = filterState.currentFilterPath,
                                    mediaDbId = video.id,
                                    filterPathId = currentFilterPathId,
                                    typeOfMedia = typeOfMedia
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
//                    IconButton(
//                        onClick = {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.path))
//                            context.startActivity(intent)
//                        },
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .padding(8.dp)
//                            .size(48.dp)
//                            .background(
//                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
//                                shape = RoundedCornerShape(24.dp)
//                            )
//                    ) {
//                        Icon(
//                            Icons.Default.Fullscreen,
//                            contentDescription = "Open in YouTube app",
//                            tint = Color.White
//                        )
//                    }
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
    filteredAlbums: List<Album>,
    albumsDisplay: List<Album>,
    onRefresh: () -> Unit = {},
    filterPath: List<FilterPath>, // new parameter
    onArtistSelected: (Artist) -> Unit = {},
    onAlbumSelected: (Album) -> Unit = {},
    scrollToAlbumsTrigger: MutableState<Int>,
    currentFilterPathId: Int?,
    minimiseMaximiseToggle: Boolean,
    currentMediaEntryTypeOfMedia: Int?
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
            filteredAlbums = filteredAlbums,
            onAlbumSelected = onAlbumSelected,
            currentMediaEntryTypeOfMedia= currentMediaEntryTypeOfMedia,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            minimiseMaximiseToggle = minimiseMaximiseToggle,
            scrollToAlbumsTrigger = scrollToAlbumsTrigger,
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
            filteredAlbums = filteredAlbums,
            onAlbumSelected = onAlbumSelected,
            currentMediaEntryTypeOfMedia= currentMediaEntryTypeOfMedia,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            minimiseMaximiseToggle = minimiseMaximiseToggle,
            scrollToAlbumsTrigger = scrollToAlbumsTrigger,
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

        // 2. 👇 NEW: Album grid below the artists
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)   // ✅ provide finite height
            ) {
                AlbumsSection(
                    onAlbumSelected = onAlbumSelected,
                    currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
                    albumsDisplay = albumsDisplay,
                    currentFilterPathId = currentFilterPathId,
                    minimiseMaximiseToggle = minimiseMaximiseToggle,
                    showMainAndFeaturedChips = false,
                    modifier = Modifier.fillMaxSize()
                )
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
    val hasVideos = artist.embadableVideoCount > 0
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

// Add this after existing composables in MainScreen.kt

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    onRefresh: () -> Unit,
    isPlayerVisible: Boolean
) {
    val enrichedHistory by viewModel.enrichedHistory.collectAsState()

    val currentPlayingDbId by playerViewModel.currentVideoDbIdState.collectAsState()

    // Load data when this screen appears
    LaunchedEffect(Unit) {
        viewModel.loadEnrichedHistory()
    }

    // Group by date (today, yesterday, older)
    val groupedByDate = remember(enrichedHistory) {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L
        enrichedHistory.groupBy { item ->
            when {
                item.timestamp >= todayStart -> "Today"
                item.timestamp >= yesterdayStart -> "Yesterday"
                else -> formatDate(item.timestamp)
            }
        }.toList().sortedByDescending { (key, _) ->
            when (key) {
                "Today" -> 3
                "Yesterday" -> 2
                else -> 1
            }
        }
    }

    // --- Sliding filter bar (collapsible) ---
    val filterBarHeightPx = remember { mutableIntStateOf(0) }
    var filterBarOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (filterBarOffset + delta).coerceIn(
                    -filterBarHeightPx.intValue.toFloat(),
                    0f
                )
                val consumed = newOffset - filterBarOffset
                filterBarOffset = newOffset
                return Offset(0f, consumed)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {

        // History List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            groupedByDate.forEach { (date, items) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(items) { historyItem ->
                    HistoryCard(
                        item = historyItem,
                        currentPlayingDbId = currentPlayingDbId,
                        isPlayerVisible = isPlayerVisible,
                        onFilterPathClick = {
                            // Restore this filter path
                            viewModel.restoreFilterPathFromGroupItem(historyItem)
                        },
                        onVideoClick = { video ->
                            // Play video in mini mode without changing filter path
                            val youtubeId = extractYouTubeVideoId(video.path)
                            if (youtubeId != null) {
                                playerViewModel.loadVideo(
                                    videoId = youtubeId,
                                    cardId = video.locationId,
                                    currentFilterPath = null, // keep current filter
                                    startInMiniMode = true,
                                    mediaDbId = video.id,
                                    filterPathId = null, // no filter path change
                                    typeOfMedia = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: MainViewModel.HistoryGroupItem,
    isPlayerVisible: Boolean,
    currentPlayingDbId: Int?,
    onFilterPathClick: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Filter path chips row (clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFilterPathClick() }
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.filterPaths.isEmpty()) {
                    // Dimmed "No Filter" chip
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(Dimens.chipRoundedCorner))
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                            )
                            .padding(horizontal = Dimens.chiptextHorizontalPadding, vertical = 6.dp)
                    ) {
                        Text(
                            text = "  *  ",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    item.filterPaths.forEach { filter ->
                        FilterPathChip(
                            text = filter.entityName,
                            isSelected = false,
                            onClick = { /* handled by parent row click */ }
                        )
                    }
                }
            }

            // Video list (only shown if isPlayerVisible is true)
            if (isPlayerVisible && item.videos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.videos.forEach { video ->
                        HistoryVideoRow(
                            video = video,
                            currentPlayingDbId = currentPlayingDbId,
                            onClick = { onVideoClick(video) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryVideoRow(
    video: Video,
    currentPlayingDbId: Int?,
    onClick: () -> Unit
) {
    val isCurrentlyPlaying = video.id == currentPlayingDbId   // direct Int comparison

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp)
            .background(
                color = if (isCurrentlyPlaying ) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                else Color.Transparent // or any default color
            ),
        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Thumbnail
        val thumbnailUrl = video.getThumbnailUrl()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = video.name,
            modifier = Modifier.size(60.dp, 45.dp),
            contentScale = ContentScale.Crop
        )
        // Title
        Text(
            text = video.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// Helper functions
private fun getStartOfDay(timestamp: Long): Long {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun ArtistImage(
    artist: Artist,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    // Generate on each composition – cheap enough
    val fallbackPainter = BitmapPainter(generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap())

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
    filteredAlbums: List<Album>,
    albumsDisplay: List<Album>,
    onAlbumSelected: (Album) -> Unit,
    scrollToAlbumsTrigger: MutableState<Int>,
    currentMediaEntryTypeOfMedia: Int?,
    currentFilterPathId:  Int?,
    minimiseMaximiseToggle: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullscreenImage by remember { mutableStateOf(false) }

    // Determine if we have a real thumbnail
    val hasThumbnail = artist.thumbnailUrl != null
    val imageHeight = if (hasThumbnail) 300.dp else 150.dp

    val listState = rememberLazyListState()     // lazy list state for scrolling

    // Scroll to albums section (index 3) when trigger changes
    LaunchedEffect(scrollToAlbumsTrigger.value) {
        if (scrollToAlbumsTrigger.value > 0) {
            // Small delay to ensure the layout is ready
            delay(100)
            listState.animateScrollToItem(4)
        }
    }

    LazyColumn(
        state = listState,
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
                        val fallbackPainter = BitmapPainter(generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap())
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

        // 4. Albums section
        // Inside SingleArtistView, replace the item for AlbumsSection with:
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)   // ✅ provide finite height
            ) {
                AlbumsSection(
                    onAlbumSelected = onAlbumSelected,
                    currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
                    albumsDisplay = albumsDisplay,
                    currentFilterPathId = currentFilterPathId,
                    minimiseMaximiseToggle = minimiseMaximiseToggle,
                    showMainAndFeaturedChips = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
                    val fallbackPainter = BitmapPainter(generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap())
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

@Composable
fun AlbumsSection(
    onAlbumSelected: (Album) -> Unit,
    currentMediaEntryTypeOfMedia: Int?,
    albumsDisplay: List<Album>,
    currentFilterPathId: Int?,
    minimiseMaximiseToggle: Boolean,
    showMainAndFeaturedChips: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {


        AlbumGridView(
            onAlbumClick = onAlbumSelected,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            minimiseMaximiseToggle = minimiseMaximiseToggle,
            showMainAndFeaturedChips = showMainAndFeaturedChips,
            modifier = modifier.fillMaxHeight()
        )
    }
}

@Composable
fun AlbumGridView(
    onAlbumClick: (Album) -> Unit,
    albumsDisplay: List<Album>,
    currentFilterPathId: Int?,
    minimiseMaximiseToggle: Boolean,
    showMainAndFeaturedChips: Boolean,
    modifier: Modifier = Modifier
) {
    // Tab state
    val mainCount = remember(albumsDisplay) { albumsDisplay.count { it.isMain == 1 } }
    val featuredCount = remember(albumsDisplay) { albumsDisplay.count { it.isMain == 0 } }

    // Shuffled vs alphabetical mode (only active when no year/rating sort)
    var isAlphabeticalMode by remember { mutableStateOf(false) }

    Log.d(TAG, "AlbumGridView recomposed with filterPathId=$currentFilterPathId, albumsDisplay.size=${albumsDisplay.size}")

    var selectedTab by remember {
        mutableStateOf(
            when {
                mainCount > 0 -> AlbumGridTab.MAIN
                featuredCount > 0 -> AlbumGridTab.FEATURED
                else -> null
            }
        )
    }

    LaunchedEffect(mainCount, featuredCount) {
        selectedTab = when {
            selectedTab == AlbumGridTab.MAIN && mainCount == 0 && featuredCount > 0 -> AlbumGridTab.FEATURED
            selectedTab == AlbumGridTab.FEATURED && featuredCount == 0 && mainCount > 0 -> AlbumGridTab.MAIN
            selectedTab == AlbumGridTab.MAIN && mainCount == 0 && featuredCount == 0 -> null
            selectedTab == AlbumGridTab.FEATURED && featuredCount == 0 && mainCount == 0 -> null
            // No tab selected, but now MAIN has albums → select MAIN
            selectedTab == null && mainCount > 0 -> AlbumGridTab.MAIN
            // No tab selected, MAIN has 0 but FEATURED has albums → select FEATURED
            selectedTab == null && featuredCount > 0 -> AlbumGridTab.FEATURED
            else -> selectedTab
        }
    }

    LaunchedEffect(albumsDisplay) {
        isAlphabeticalMode = false   // back to shuffled after refresh
    }

    // Sorting state
    var yearSort by remember { mutableStateOf<SortDirection?>(null) }      // start with no sort
    var ratingSort by remember { mutableStateOf<SortDirection?>(null) }

    fun setYearSort(direction: SortDirection?) {
        yearSort = direction
        if (direction != null) ratingSort = null
        isAlphabeticalMode = false   // ← add this
    }

    fun setRatingSort(direction: SortDirection?) {
        ratingSort = direction
        if (direction != null) yearSort = null
        isAlphabeticalMode = false   // ← add this
    }

    val filteredByTab = remember(albumsDisplay, selectedTab) {
        when (selectedTab) {
            AlbumGridTab.MAIN -> albumsDisplay.filter { it.isMain == 1 }
            AlbumGridTab.FEATURED -> albumsDisplay.filter { it.isMain == 0 }
            null -> emptyList()
        }
    }

    // Helper to move null-thumbnail albums to the end while preserving order within groups
    fun List<Album>.moveNullThumbnailsToEnd(): List<Album> {
        val (withThumb, withoutThumb) = partition { it.youtubeVideoIdForThumbnail != null }
        return withThumb + withoutThumb
    }

    // Sorted albums: priority rating > year > alphabetical
    val sortedAlbums = remember(filteredByTab, yearSort, ratingSort, isAlphabeticalMode) {
        Log.d(TAG, "🔄 Sorted albums recalculated, new size=${filteredByTab.size}")
        when {
            ratingSort != null -> {
                // Primary: ratingAverage, Secondary: ratingCount
                val avgComparator = compareBy<Album, Double?>(nullsLast()) { it.ratingAverage }
                val countComparator = compareBy<Album, Int?>(nullsLast()) { it.ratingCount }
                val combined = avgComparator.then(countComparator)
                val comparator = if (ratingSort == SortDirection.ASC) combined else combined.reversed()
                filteredByTab.sortedWith(comparator)
            }
            yearSort != null -> {
                val baseComparator = compareBy<Album, Int?>(nullsLast()) { it.year }
                val comparator = if (yearSort == SortDirection.ASC) baseComparator else baseComparator.reversed()
                filteredByTab.sortedWith(comparator)
            }
            else -> {
                if (isAlphabeticalMode) {
                    filteredByTab.sortedWith(compareBy { it.title?.lowercase() ?: "" })
                } else {
                    filteredByTab.moveNullThumbnailsToEnd()
                }
            }
        }
    }

    val hasAlbums = sortedAlbums.isNotEmpty()

    // ========== YEAR NOTE STATE & TIMER ==========
    var currentNoteText by remember { mutableStateOf("") }
    var showYearNote by remember { mutableStateOf(false) }

    // Auto‑hide after 3 seconds
    LaunchedEffect(currentNoteText) {
        if (currentNoteText.isNotBlank()) {
            showYearNote = true
            delay(3000)
            showYearNote = false
        }
    }

    // Helper to generate note text based on current sort mode and album
    fun getNoteText(album: Album): String {
        return when {
            ratingSort != null -> {
                val avg = album.ratingAverage
                val count = album.ratingCount ?: 0
                val avgStr = if (avg != null) DecimalFormat("#.##").format(avg) else "?"
                "$avgStr ($count)"
            }
            yearSort != null -> {
                album.year?.toString() ?: ""
            }
            else -> ""   // ← no note when no sort chip active
        }
    }

    // Update note from an album
    fun updateNoteFromAlbum(album: Album?) {
        if (album != null) {
            currentNoteText = getNoteText(album)
            Log.d(TAG, "Note updated: mode=${if (ratingSort != null) "rating" else if (yearSort != null) "year" else "alpha"}, text=$currentNoteText, album=${album.title}")
        } else {
            currentNoteText = ""
        }
    }

    // ========== GRID SCROLL STATE ==========
    val gridState = remember(currentFilterPathId, albumsDisplay.size) {
        Log.d(TAG, "🔄 Creating NEW LazyGridState! filterPathId=$currentFilterPathId, size=${albumsDisplay.size}")
        LazyGridState()
    }
    val coroutineScope = rememberCoroutineScope()

    // Observe first visible item and update note on any scroll
    LaunchedEffect(gridState.firstVisibleItemIndex, ratingSort, yearSort) {
        if (sortedAlbums.isNotEmpty()) {
            val index = gridState.firstVisibleItemIndex.coerceIn(0, sortedAlbums.lastIndex)
            val album = sortedAlbums.getOrNull(index)
            updateNoteFromAlbum(album)
        }
    }

    // Log when the grid state's first visible item changes
    LaunchedEffect(gridState.firstVisibleItemIndex) {
        Log.d(TAG, "Grid first visible index changed to: ${gridState.firstVisibleItemIndex}")
    }

    // ========== DOT SCROLLBAR ==========
    val albumsPerDot = 6
    val dotCount = if (sortedAlbums.isEmpty()) 1 else {
        (sortedAlbums.size + (albumsPerDot - 1) ) / albumsPerDot  // ceil division
    }

    val activeDotIndex = if (sortedAlbums.isEmpty() || dotCount <= 1) {
        0
    } else {
        val firstVisibleIndex = gridState.firstVisibleItemIndex
        val group = firstVisibleIndex / albumsPerDot
        group.coerceIn(0, dotCount - 1)
    }

        fun scrollToDot(dotIdx: Int) {
        val targetIndex = (dotIdx * albumsPerDot).coerceAtMost(sortedAlbums.lastIndex)
        coroutineScope.launch {
            gridState.scrollToItem(targetIndex)
        }
    }

    fun scrollToItemIndex(index: Int) {
        Log.d(TAG, "scrollToItemIndex requested: index=$index, sortedAlbums.size=${sortedAlbums.size}")
        if (index in sortedAlbums.indices) {
            coroutineScope.launch {
                Log.d(TAG, "Executing scrollToItem($index)")
                gridState.scrollToItem(index)
                delay(100)
                Log.d(TAG, "After scroll: firstVisibleItemIndex=${gridState.firstVisibleItemIndex}")
            }
        } else {
            Log.w(TAG, "scrollToItemIndex: index $index out of bounds (0..${sortedAlbums.lastIndex})")
        }
    }

    fun scrollToGroup(dotIdx: Int) {
        val targetIndex = (dotIdx * albumsPerDot).coerceAtMost(sortedAlbums.lastIndex)
        coroutineScope.launch {
            gridState.scrollToItem(targetIndex)
        }
    }

    fun onDotClicked(dotIdx: Int) {
        if (sortedAlbums.isEmpty()) return
        if (yearSort == null && ratingSort == null) {
            isAlphabeticalMode = true
        }
        scrollToGroup(dotIdx)
        val album = sortedAlbums.getOrNull(dotIdx * albumsPerDot)
        updateNoteFromAlbum(album)
    }

    fun onDotDragged(dotIdx: Int, isDragging: Boolean) {
        if (isDragging) {
            if (yearSort == null && ratingSort == null) {
                isAlphabeticalMode = true
            }
            scrollToGroup(dotIdx)
            val album = sortedAlbums.getOrNull(dotIdx * albumsPerDot)
            updateNoteFromAlbum(album)
        }
    }

    // ========== UI ==========
    Column(modifier = modifier.fillMaxSize()) {
        AlbumFilterChipsRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            mainCount = mainCount,
            featuredCount = featuredCount,
            hasAlbums = hasAlbums,
            yearSort = yearSort,
            ratingSort = ratingSort,
            setYearSort = ::setYearSort,
            showMainAndFeaturedChips = showMainAndFeaturedChips,
            setRatingSort = ::setRatingSort
        )

        // Horizontal grid with floating note
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (sortedAlbums.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No albums to display")
                }
            } else {
                val fixedGridCells = if (minimiseMaximiseToggle) 2 else 1

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(fixedGridCells),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val cardWidth = if (minimiseMaximiseToggle) 120.dp else 250.dp

                    items(sortedAlbums) { album ->
                        AlbumCard(
                            album = album,
                            modifier = Modifier
                                .width(cardWidth)
                                .animateContentSize(),
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }

                // Floating note chip (always shown on scroll, content depends on sort mode)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showYearNote && currentNoteText.isNotBlank() && (yearSort != null || ratingSort != null),
                    //enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(currentNoteText) },
                        modifier = Modifier
                            .padding(16.dp)
                            .zIndex(1f),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
        // State for scrubbing (interacting with dots)
        var isScrubbing by remember { mutableStateOf(false) }
        var scrubYear by remember { mutableStateOf("") }

        fun getYearString(album: Album): String = album.year?.toString() ?: ""

        fun updateScrubYearForIndex(index: Int) {
            if (index in sortedAlbums.indices) {
                val album = sortedAlbums[index]
                scrubYear = getYearString(album)
                Log.d(TAG, "Scrub index=$index, album=${album.title}, year=${album.year}")
            } else {
                scrubYear = ""
                Log.w(TAG, "Scrub index out of range: $index")
            }
        }


        fun onDotHover(dotIdx: Int, isDragging: Boolean) {
            if (isDragging) {
                val targetIndex = if (dotCount <= 1) 0 else {
                    (dotIdx.toFloat() / (dotCount - 1) * (sortedAlbums.size - 1)).roundToInt()
                }
                scrollToItemIndex(targetIndex)
                updateScrubYearForIndex(targetIndex)
                isScrubbing = true
            }
        }

        // Custom dot scrollbar (only if there are albums)
        if (sortedAlbums.isNotEmpty() && dotCount > 1) {
            DotScrollbar(
                dotCount = dotCount,
                activeDotIndex = activeDotIndex,
                onDotClick = { dotIdx -> onDotClicked(dotIdx) },
                onDotDrag = { dotIdx, isDragging ->
                        onDotHover(dotIdx, isDragging)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else if (sortedAlbums.isNotEmpty() && dotCount == 1) {
            // Single dot (no interaction needed) – just show a single dot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun DotScrollbar(
    dotCount: Int,
    activeDotIndex: Int,
    onDotClick: (Int) -> Unit,
    onDotDrag: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use a Row with dots spaced evenly
    // To support dragging, we can use a pointerInput on the whole row and calculate which dot the pointer is over.
    Row(
        modifier = modifier
            .height(10.dp)
            .pointerInput(Unit) {
                // Detect drag events and call onDotDrag with the nearest dot
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.firstOrNull()?.position ?: continue
                        val dotWidth = size.width / dotCount
                        val dotIndex = (position.x / dotWidth).toInt().coerceIn(0, dotCount - 1)
                        val isDragging = event.changes.any { it.pressed }
                        onDotDrag(dotIndex, isDragging)
                        if (isDragging && event.changes.all { !it.pressed }) {
                            // when drag ends, call with false
                            onDotDrag(dotIndex, false)
                        }
                    }
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until dotCount) {
            val isActive = i == activeDotIndex
            Box(
                modifier = Modifier
                    .size(if (isActive) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    .clickable { onDotClick(i) }
            )
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val thumbnailUrl = album.getThumbnailUrl()

    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Square thumbnail (unchanged)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_error)
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = album.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ Fixed row: year takes only needed width, artist fills rest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp) // small breathing gap
            ) {
                // Year – no weight, wraps content
                Text(
                    text = album.released?.take(4) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )

                // Artist name – takes remaining space, starts right after year
                Text(
                    text = album.artistFullName ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),//instrumentColor(album.artistInstrumentId ?: 0).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f) // fills rest, no gap before it
                )
            }
        }
    }
}

@Composable
fun AlbumFilterChipsRow(
    selectedTab: AlbumGridTab?,
    onTabSelected: (AlbumGridTab) -> Unit,
    showMainAndFeaturedChips: Boolean,
    mainCount: Int,
    featuredCount: Int,
    hasAlbums: Boolean,
    yearSort: SortDirection?,
    ratingSort: SortDirection?,
    setYearSort: (SortDirection?) -> Unit,
    setRatingSort: (SortDirection?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showMainAndFeaturedChips) {
            FilterChip(
                selected = selectedTab == AlbumGridTab.MAIN,
                onClick = { onTabSelected(AlbumGridTab.MAIN) },
                label = { Text("Main") },
                enabled = mainCount > 0
            )
            FilterChip(
                selected = selectedTab == AlbumGridTab.FEATURED,
                onClick = { onTabSelected(AlbumGridTab.FEATURED) },
                label = { Text("Featured") },
                enabled = featuredCount > 0
            )
        }
        if (hasAlbums) {
            // Year sorting chip
            FilterChip(
                selected = yearSort != null,
                onClick = {
                    when (yearSort) {
                        null -> setYearSort(SortDirection.ASC)
                        SortDirection.ASC -> setYearSort(SortDirection.DESC)
                        SortDirection.DESC -> setYearSort(null)
                    }
                },
                label = { Text("Year") },
                leadingIcon = if (yearSort != null) {
                    {
                        Icon(
                            if (yearSort == SortDirection.ASC) Icons.Default.ArrowUpward
                            else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
            // Rating sorting chip
            FilterChip(
                selected = ratingSort != null,
                onClick = {
                    when (ratingSort) {
                        null -> setRatingSort(SortDirection.ASC)
                        SortDirection.ASC -> setRatingSort(SortDirection.DESC)
                        SortDirection.DESC -> setRatingSort(null)
                    }
                },
                label = { Text("Rating") },
                leadingIcon = if (ratingSort != null) {
                    {
                        Icon(
                            if (ratingSort == SortDirection.ASC) Icons.Default.ArrowUpward
                            else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}