    
    package com.example.jazzlibraryktroomjpcompose.ui.main
    
    import android.app.Activity
    import androidx.activity.compose.BackHandler
    import androidx.compose.foundation.layout.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.unit.dp
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.compose.animation.core.animateDpAsState
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.material.icons.Icons
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.foundation.layout.Box
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextButton
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Snackbar
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.style.TextOverflow
    import com.example.jazzlibraryktroomjpcompose.domain.models.Video
    import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.compose.ui.input.nestedscroll.nestedScroll
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    import androidx.compose.ui.input.nestedscroll.NestedScrollSource
    import androidx.compose.ui.platform.LocalDensity
    import androidx.compose.ui.unit.IntOffset
    import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
    import kotlin.math.roundToInt
    import androidx.compose.animation.core.Animatable
    import androidx.compose.foundation.gestures.detectTapGestures
    import androidx.compose.foundation.lazy.LazyListState
    import androidx.compose.foundation.lazy.rememberLazyListState
    import androidx.compose.material.icons.filled.PlayArrow
    import androidx.compose.material3.pulltorefresh.PullToRefreshBox
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.layout.positionInRoot
    import androidx.compose.ui.unit.IntSize
    import androidx.compose.ui.zIndex
    import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
    import kotlinx.coroutines.launch
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import coil.compose.AsyncImage
    import coil.request.ImageRequest
    import com.example.jazzlibraryktroomjpcompose.R
    import com.example.jazzlibraryktroomjpcompose.ui.common.player.SmartYoutubePlayerHost
    import androidx.compose.ui.platform.LocalConfiguration
    import androidx.compose.runtime.mutableStateOf
    import android.content.res.Configuration
    import android.util.Log
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import com.google.accompanist.systemuicontroller.rememberSystemUiController
    import androidx.compose.ui.platform.LocalView
    import androidx.compose.ui.platform.LocalFocusManager
    import androidx.compose.ui.platform.LocalSoftwareKeyboardController
    import com.example.jazzlibraryktroomjpcompose.domain.models.Song
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import com.example.jazzlibraryktroomjpcompose.ui.bottomsheet.YouTubeLikeBottomSheet
    import com.example.jazzlibraryktroomjpcompose.ui.leftdrawer.LeftDrawer
    import com.example.jazzlibraryktroomjpcompose.ui.search.SmartSearchBar
    import com.example.jazzlibraryktroomjpcompose.ui.common.util.*
    import com.example.jazzlibraryktroomjpcompose.ui.common.components.*
    import com.example.jazzlibraryktroomjpcompose.ui.common.player.*
    import com.example.jazzlibraryktroomjpcompose.ui.artist.*
    import com.example.jazzlibraryktroomjpcompose.ui.video.*
    import com.example.jazzlibraryktroomjpcompose.ui.album.*
    import com.example.jazzlibraryktroomjpcompose.ui.history.*
    import com.example.jazzlibraryktroomjpcompose.domain.models.Album
    import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
    import com.example.jazzlibraryktroomjpcompose.ui.auth.LoginScreen
    import com.example.jazzlibraryktroomjpcompose.ui.about.AboutScreen
    import androidx.compose.foundation.gestures.awaitEachGesture
    import androidx.compose.foundation.gestures.awaitFirstDown
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.ui.graphics.Color
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.Cast
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.Language
    import androidx.compose.material.icons.filled.Share
    import androidx.compose.ui.graphics.RectangleShape
    import androidx.compose.animation.core.animateOffsetAsState
    import androidx.compose.animation.core.spring
    import androidx.compose.foundation.gestures.detectDragGestures
    import androidx.compose.material3.Card
    import androidx.compose.ui.geometry.Rect
    import androidx.compose.ui.layout.positionInRoot
    import kotlinx.coroutines.delay

    enum class AlbumGridTab { MAIN, FEATURED }
    enum class SortDirection { ASC, DESC }
    
    private const val TAG = "AlbumGridView"

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
    
        val context = LocalContext.current
        val isRefreshing by viewModel.isRefreshing.collectAsState()
    
        val playerStableState by playerViewModel.stableState.collectAsState()
        var activeCardRelativePosition by remember { mutableStateOf<IntOffset?>(null) }
        var activeCardSize by remember { mutableStateOf<IntSize?>(null) }
        var contentBoxRootPosition by remember { mutableStateOf(IntOffset.Zero) }
    
    
        //i have made the isPlayerVisible global (placed it in the viewmodel) so to access it independently
        val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()
    
        //witch tab is the main tab
        val currentTab by viewModel.currentTab.collectAsState()
        val hasArtistFilter = filterState.currentFilterPath.any { it.categoryId == 2 }

        val scrollLockState =
            remember { ScrollLockState() }   // That is for the singleartistvie's wikidatacard scrolling, it locks the scrolling in order for items to consume the whole scrolling gesture
    
        //orientation detection
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isFullscreen = isLandscape && playerStableState.isVisible
    
        var miniPlayerHeight by remember { mutableStateOf(0.dp) }
    
        // --- System UI control for fullscreen ---
        val systemUiController = rememberSystemUiController()
        val showBars by viewModel.showBars.collectAsState()
    
        val density = LocalDensity.current
        val topTapThresholdPx = remember { with(density) { 80.dp.toPx() } }
    
        val view = LocalView.current
    
        val listState = rememberLazyListState()
    
        val currentFilterPathId by viewModel.currentFilterPathId.collectAsState()
    
        val currentMediaEntry by playerViewModel.currentFilterPathMedia.collectAsState()

        /* v1.0.0: Albums hidden — scrollToAlbumsTrigger unused. Restore with onAlbumSelected. */
        //val scrollToAlbumsTrigger = remember { mutableStateOf(0) }
    
        var hideSearchDropdown by remember { mutableStateOf(false) }
    
        var isDropdownOpen by remember { mutableStateOf(false) }
    
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
    
        val coroutineScope = rememberCoroutineScope()

        var showLoginScreen by remember { mutableStateOf(false) }
        if (showLoginScreen) {
            LoginScreen(
                onLoginSuccess = {
                    showLoginScreen = false
                    // User is now logged in - main screen will refresh automatically
                    // because AuthState changed
                },
                onNavigateBack = { showLoginScreen = false }
            )
            return  // Don't show main screen
        }

        var showAboutScreen by remember { mutableStateOf(false) }
        if (showAboutScreen) {
            AboutScreen(onNavigateBack = { showAboutScreen = false })
            return
        }

        LaunchedEffect(Unit) {
            playerViewModel.clearBoundsEvent.collect {
                activeCardRelativePosition = null
                activeCardSize = null
            }
        }
    
        LaunchedEffect(isPlayerVisible) {
            playerViewModel.onGlobalPlayerVisibilityChanged(isPlayerVisible)
        }
    
        LaunchedEffect(isLandscape, playerStableState.isVisible) {
            viewModel.setFullscreen(isLandscape && playerStableState.isVisible)
        }

        LaunchedEffect(showBars, isFullscreen) {
            viewModel.startAutoHideTimer()
        }
    
        // Apply system UI visibility and colors based on fullscreen state
        // Then replace the system UI control LaunchedEffect with this:
        LaunchedEffect(isFullscreen, showBars) {
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

        BackHandler {
            when {
                showLoginScreen -> showLoginScreen = false
                showAboutScreen -> showAboutScreen = false
                leftDrawerState == DrawerState.OPEN -> viewModel.toggleLeftDrawer()
                else -> {
                    hideSearchDropdown = true
                    viewModel.handleBackPress { (context as? Activity)?.finish() }
                }
            }
        }
    
        if (loadingState == LoadingState.LOADING && uiState.videos.isEmpty()) {
            LoadingScreen()
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                // --- Chips row measurement ---
                val chipsHeightPx = remember { mutableIntStateOf(0) }
                val toolbarHeightPx = remember { mutableIntStateOf(0) }
                val toolbarOffset = remember { mutableFloatStateOf(0f) }
                val expandToolbar = { toolbarOffset.floatValue = 0f }
    
                val nestedScrollConnection = remember(scrollLockState, isDropdownOpen) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // If the search dropdown is open, do NOT move the toolbar
                            if (isDropdownOpen) {
                                return Offset.Zero
                            }
                            // Original logic for wiki card lock and toolbar movement
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
                /* v1.0.0: Albums hidden — albumsToShow unused. Restore with AlbumsListContent. */
                //val albumsToShow = uiState.filteredAlbums
                /* v1.0.0: Albums hidden — albumsListState unused. Restore with AlbumsListContent. */
                //val albumsListState = rememberLazyListState()

                // Scroll to the currently playing video (if any)
                fun scrollToPlayingVideo() {
                    if (playerStableState.currentTypeOfMedia == 0) {
                        val currentYoutubeId = playerStableState.currentVideoId
                        if (currentYoutubeId != null) {
                            val index = videosToShow.indexOfFirst { video ->
                                extractYouTubeVideoId(video.path) == currentYoutubeId
                            }
                            if (index != -1) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                    }
                }

                /* v1.0.0: Albums hidden — scrollToPlayingAlbum unused. Restore when Albums tab returns. */
                // Scroll to the currently playing album (if any)
//                fun scrollToPlayingAlbum() {
//                    if (playerStableState.currentTypeOfMedia == 1) {
//                        val activeCardId = playerStableState.activeCardId
//                        if (activeCardId != null && activeCardId.startsWith("album_")) {
//                            val albumId = activeCardId.removePrefix("album_").toIntOrNull()
//                            if (albumId != null) {
//                                val index = uiState.filteredAlbums.indexOfFirst { it.albumId == albumId }
//                                if (index != -1) {
//                                    coroutineScope.launch {
//                                        albumsListState.animateScrollToItem(index)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

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
                            viewModel.triggerShuffle()
    
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
                                    contentBoxRootPosition = IntOffset(
                                        x = coordinates.positionInRoot().x.roundToInt(),
                                        y = coordinates.positionInRoot().y.roundToInt()
                                    )
                                }
                        ) {

                            if (isDropdownOpen) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                        .zIndex(5f)
                                )
                            }

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
                                    onVideoTabClick = { scrollToPlayingVideo() },
                                    //v1.0.0: Albums tab hidden — onAlbumTabClick is now a no-op.
                                    onAlbumTabClick = { /* no-op */ },  //onAlbumTabClick = { scrollToPlayingAlbum() },
                                    onExpandToolbar = expandToolbar,
                                    onTabSelected = { tab ->
                                        viewModel.setCurrentTab(tab)          // MainViewModel
                                        playerViewModel.setCurrentTab(tab)    // PlayerViewModel (new)
                                    },
                                    hideSearchDropdown = hideSearchDropdown,
                                    onDropdownVisibilityChanged = { isDropdownOpen = it },
                                    onSearchBarClicked = { hideSearchDropdown = false }
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
                                            onRefresh = { viewModel.safeRefreshDataFromAPI() },
                                            onActiveCardBoundsChanged = { cardId, rootPosition, size ->
                                                if (cardId == playerStableState.activeCardId) {
                                                    val relativePos =
                                                        rootPosition - contentBoxRootPosition
                                                    activeCardRelativePosition = relativePos
                                                    activeCardSize = size
                                                }
                                            },
                                            onCardTitleClick = { videoId ->
                                                viewModel.onCardTitleClick(
                                                    videoId
                                                )
                                            }
                                        )
//                                            * ALBUMS SCREEN HIDDEN FOR v1.0.0 GOOGLE PLAY RELEASE
//                                            * ============================================================
//                                            * AlbumsListContent is never rendered in this release.
//                                            * The full original block is preserved below for future restore.
//                                            *
//                                        MainTab.ALBUMS -> {
//                                            // ✅ No LaunchedEffect – no auto‑selection of first album
//                                            AlbumsListContent(
//                                                playerViewModel = playerViewModel,
//                                                listState = albumsListState,
//                                                viewModel = viewModel,
//                                                onActiveCardBoundsChanged = { cardId, position, size ->
//                                                    if (cardId == playerStableState.activeCardId) {
//                                                        val relativePos = position - contentBoxRootPosition
//                                                        activeCardRelativePosition = relativePos
//                                                        activeCardSize = size
//                                                    }
//                                                },
//                                                onRefresh = { viewModel.safeRefreshDataFromAPI() }
//                                            )
//                                        }
                                        MainTab.ALBUMS -> { /* hidden for v1.0.0 — nothing renders */
                                        }


                                        MainTab.ARTISTS -> ArtistContent(
                                            modifier = Modifier.fillMaxSize(),
                                            onRefresh = { viewModel.shuffleArtists() },
                                            onArtistSelected = { artist ->
                                                viewModel.handleChipSelection(
                                                    FilterPath.CATEGORY_ARTIST,
                                                    artist.id,
                                                    artist.fullName,
                                                    true // add filter
                                                )
                                            },
                                            onAlbumSelected = { album ->
//                                                * v1.0.0: Albums hidden everywhere.
//                                                * Album-card tap inside Artists screen is now a no-op.
//                                                * Full original behavior preserved below for future restore.
//                                                val alreadyFiltered =
//                                                    filterState.currentFilterPath.any {
//                                                        it.categoryId == FilterPath.CATEGORY_ARTIST && it.entityId == album.artistId
//                                                    }
//                                                if (!alreadyFiltered && album.artistId != null) {
//                                                    viewModel.handleChipSelection(
//                                                        FilterPath.CATEGORY_ARTIST,
//                                                        album.artistId,
//                                                        album.artistFullName ?: "Unknown Artist",
//                                                        true
//                                                    )
//                                                }
//                                                // Always scroll to albums section after clicking an album card
//                                                scrollToAlbumsTrigger.value++
                                            },
                                            playerViewModel = playerViewModel,
                                            viewModel = viewModel
                                        )

                                        MainTab.HISTORY -> HistoryContent(
                                            modifier = Modifier.fillMaxSize(),
                                            viewModel = viewModel,
                                            playerViewModel = playerViewModel,
                                            onRefresh = { viewModel.refreshHistory() }
                                        )
                                    }
                                }
                            }

                            // ----- PLAYER (draggable mini player) -----
                            // ----- PLAYER -----
                            if (playerStableState.isVisible) {
                                val density = LocalDensity.current

                                val isMiniPlayer = playerStableState.isInMiniMode && !isFullscreen
                                var miniPlayerDragOffset by remember { mutableStateOf(Offset.Zero) }

                                LaunchedEffect(isMiniPlayer) {
                                    if (isMiniPlayer) {
                                        miniPlayerDragOffset = Offset.Zero
                                    }
                                }

                                val playerModifier = when {
                                    isFullscreen -> Modifier
                                        .fillMaxSize()
                                        .zIndex(10f)
                                        .graphicsLayer { clip = true }
                                        .pointerInput(Unit) {
                                            detectTapGestures { offset ->
                                                if (offset.y < topTapThresholdPx) {
                                                    viewModel.setShowBars(true)
                                                }
                                            }
                                        }

                                    isMiniPlayer -> Modifier
                                        .padding(end = 6.dp, bottom = 6.dp)
                                        .size(width = 300.dp, height = 200.dp)
                                        .align(Alignment.BottomEnd)
                                        .graphicsLayer {
                                            translationX = miniPlayerDragOffset.x
                                            translationY = miniPlayerDragOffset.y
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

                                val miniCardDragModifier = if (isMiniPlayer) {
                                    Modifier.pointerInput(
                                        playerStableState.playerInstanceId,
                                        currentFilterPathId
                                    ) {
                                        val horizontalPaddingPx = with(density) { 8.dp.toPx() }
                                        val verticalPaddingPx = with(density) { 3.dp.toPx() }
                                        val controlsWidthPx = with(density) { 40.dp.toPx() }
                                        val controlsGapPx = with(density) { 4.dp.toPx() }
                                        val dismissThresholdPx = with(density) { 110.dp.toPx() }

                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            // The video area is deliberately excluded from dragging. The card
                                            // padding and the control strip remain available as drag surfaces.
                                            val videoLeft = horizontalPaddingPx
                                            val videoRight =
                                                size.width - horizontalPaddingPx - controlsWidthPx - controlsGapPx
                                            val videoTop = verticalPaddingPx
                                            val videoBottom = size.height - verticalPaddingPx

                                            val startedOnVideo =
                                                down.position.x >= videoLeft &&
                                                        down.position.x <= videoRight &&
                                                        down.position.y >= videoTop &&
                                                        down.position.y <= videoBottom

                                            if (startedOnVideo) {
                                                // Observe the gesture without consuming it, so the YouTube
                                                // player continues to receive taps and its own gestures.
                                                var pointerStillDown = true
                                                while (pointerStillDown) {
                                                    val event = awaitPointerEvent()
                                                    val change =
                                                        event.changes.firstOrNull { it.id == down.id }
                                                    pointerStillDown = change?.pressed == true
                                                }
                                            } else {
                                                var totalDrag = Offset.Zero
                                                var dragging = false
                                                var pointerStillDown = true

                                                while (pointerStillDown) {
                                                    val event = awaitPointerEvent()
                                                    val change =
                                                        event.changes.firstOrNull { it.id == down.id }

                                                    if (change == null || !change.pressed) {
                                                        pointerStillDown = false
                                                    } else {
                                                        val delta = change.position - change.previousPosition
                                                        totalDrag += delta

                                                        if (!dragging && totalDrag.getDistance() > viewConfiguration.touchSlop) {
                                                            dragging = true
                                                        }

                                                        if (dragging) {
                                                            miniPlayerDragOffset += delta
                                                            change.consume()
                                                        }
                                                    }
                                                }

                                                if (dragging) {
                                                    if (totalDrag.y >= dismissThresholdPx) {
                                                        miniPlayerDragOffset = Offset.Zero
                                                        playerViewModel.closePlayer(
                                                            currentFilterPathId
                                                        )
                                                        // Closing the player makes controlsAccessible false;
                                                        // the existing pager effect returns the pager to page 2.
                                                    } else {
                                                        // A short drag returns the card to its corner.
                                                        miniPlayerDragOffset = Offset.Zero
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Modifier
                                }

                                Card(
                                    modifier = playerModifier
                                        .then(miniCardDragModifier)
                                        .onGloballyPositioned { coordinates ->
                                            miniPlayerHeight = with(density) {
                                                coordinates.size.height.toDp()
                                            }
                                        },
                                    shape = if (isMiniPlayer) {
                                        RoundedCornerShape(12.dp)
                                    } else {
                                        RectangleShape
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMiniPlayer) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            Color.Transparent
                                        }
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (isMiniPlayer) 4.dp else 0.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(
                                                if (isMiniPlayer) {
                                                    Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 3.dp
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Keep exactly one player host and keep its key independent of mini mode.
                                        SmartYoutubePlayerHost(
                                            key = playerStableState.playerInstanceId,
                                            videoId = playerStableState.currentVideoId,
                                            isFullscreen = isFullscreen,
                                            isMiniMode = isMiniPlayer,
                                            onPlayerReady = { player ->
                                                playerViewModel.setPlayer(player)
                                            },
                                            onWebViewReady = { webView ->
                                                webView.post {
                                                    webView.setPadding(0, 0, 0, 0)
                                                    webView.isScrollContainer = false
                                                    webView.isVerticalScrollBarEnabled = false
                                                    webView.isHorizontalScrollBarEnabled = false
                                                    webView.setInitialScale(100)
                                                    webView.layoutParams =
                                                        webView.layoutParams.apply {
                                                            width =
                                                                ViewGroup.LayoutParams.MATCH_PARENT
                                                            height =
                                                                ViewGroup.LayoutParams.MATCH_PARENT
                                                        }
                                                    webView.requestLayout()
                                                }
                                            },
                                            onVideoEnded = {
                                                playerViewModel.nextVideo(
                                                    startInMiniMode = currentTab != MainTab.VIDEOS
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )

                                        if (isMiniPlayer) {
                                            Spacer(modifier = Modifier.width(4.dp))

                                            Column(
                                                modifier = Modifier
                                                    .width(35.dp)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.SpaceEvenly,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                MiniPlayerControl(
                                                    icon = Icons.Default.Close,
                                                    description = "Close mini player",
                                                    onClick = {
                                                        playerViewModel.closePlayer(
                                                            currentFilterPathId
                                                        )
                                                    }
                                                )

                                                MiniPlayerControl(
                                                    icon = Icons.Default.ArrowBack,
                                                    description = "Back",
                                                    onClick = {}
                                                )

                                                MiniPlayerControl(
                                                    icon = Icons.Default.Cast,
                                                    description = "Cast",
                                                    onClick = {}
                                                )

                                                MiniPlayerControl(
                                                    icon = Icons.Default.Share,
                                                    description = "Share",
                                                    onClick = {}
                                                )

                                                MiniPlayerControl(
                                                    icon = Icons.Default.Language,
                                                    description = "Internet",
                                                    onClick = {}
                                                )
                                            }
                                        } else if (isFullscreen) {
                                            FullscreenControlsColumn(
                                                onShare = { Toast.makeText(context, "Share - to be implemented", Toast.LENGTH_SHORT).show() },
                                                onCast = { Toast.makeText(context, "Cast - to be implemented", Toast.LENGTH_SHORT).show() },
                                                onBack = { Toast.makeText(context, "ReturnToThePlayerCard - to be implemented", Toast.LENGTH_SHORT).show() },
                                                onPrevious = { playerViewModel.previousVideo() },
                                                onNext = { playerViewModel.nextVideo() },
                                                onClose = {
                                                    playerViewModel.closePlayer(currentFilterPathId)
                                                },
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



                }

                // ----- LEFT DRAWER with scrim -----
                if (leftDrawerState == DrawerState.OPEN) {
                    // Scrim - fills whole screen and closes drawer on tap
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.toggleLeftDrawer()
                            }
                            .zIndex(7f)
                    )

                    // Drawer content
                    LeftDrawer(
                        isOpen = true,
                        onClose = {  },
                        onRefreshClick = { viewModel.safeRefreshDataFromAPI() },
                        onClearHistoryClick = {
                            playerViewModel.closePlayer()
                            viewModel.clearHistory()
                        },
                        onLoginClick = {
                            showLoginScreen = true
                            //viewModel.toggleLeftDrawer()
                        },
                        onAboutClick = {
                            showAboutScreen = true
                            //viewModel.toggleLeftDrawer()
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp)
                            .offset(x = leftDrawerOffset)
                            .zIndex(8f)
                    )
                }

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
    fun toolbarBox(
        onTabSelected: (MainTab) -> Unit,
        hideSearchDropdown: Boolean,
        onSearchBarClicked: () -> Unit,
        onDropdownVisibilityChanged: (Boolean) -> Unit,
        onVideoTabClick: () -> Unit,
        onAlbumTabClick: () -> Unit,
        onExpandToolbar: () -> Unit,
    ) {

        SmartSearchBar(
            hideSearchDropdown = hideSearchDropdown,
            onExpandToolbar = onExpandToolbar,
            onSearchBarClicked = {
                onSearchBarClicked()
            },
            onDropdownVisibilityChanged = onDropdownVisibilityChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
        // rest of the function unchanged
        VideoStatsRow(
            onVideoTabClick = onVideoTabClick,
            onAlbumTabClick = onAlbumTabClick,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun SongListItem(
        song: Song,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val thumbnailUrl = song.getThumbnailUrl()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = song.songTitle,
                modifier = Modifier.size(48.dp, 36.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_error)
            )
            Text(
                text = song.songTitle ?: "Untitled",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play song",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    @Composable
    private fun MiniPlayerControl(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        description: String,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }