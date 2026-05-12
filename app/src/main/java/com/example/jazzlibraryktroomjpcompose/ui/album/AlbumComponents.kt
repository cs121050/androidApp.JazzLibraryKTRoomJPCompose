package com.example.jazzlibraryktroomjpcompose.ui.album

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jazzlibraryktroomjpcompose.R
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerStableState
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlaylistItem
import com.example.jazzlibraryktroomjpcompose.ui.artist.ArtistImage
import com.example.jazzlibraryktroomjpcompose.ui.common.components.DotsRow
import com.example.jazzlibraryktroomjpcompose.ui.common.components.LocalScrollLock
import com.example.jazzlibraryktroomjpcompose.ui.common.components.ScrollLockState
import com.example.jazzlibraryktroomjpcompose.ui.common.player.PlayerCardVisibilityMonitor
import com.example.jazzlibraryktroomjpcompose.ui.main.AlbumGridTab
import com.example.jazzlibraryktroomjpcompose.ui.main.MainViewModel
import com.example.jazzlibraryktroomjpcompose.ui.main.SortDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@Composable
fun AlbumsListContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),

    listState: LazyListState,
    minimiseMaximiseToggle: Boolean = true,
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    onRefresh: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val currentFilterPathId by viewModel.currentFilterPathId.collectAsState()
    val playerUiState by playerViewModel.stableState.collectAsState()
    val albumArtistsMap by viewModel.albumArtistsMap.collectAsState()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()
    val currentPlayingSongId by playerViewModel.currentVideoDbIdState.collectAsState()

    val currentMediaEntry by playerViewModel.currentFilterPathMedia.collectAsState()
    val currentMediaEntryTypeOfMedia = currentMediaEntry?.typeOfMedia

    // Now extract the needed values
    val albums = uiState.filteredAlbums
    val filterPath = filterState.currentFilterPath

    LaunchedEffect(albums) {
        if (albums.isEmpty()) {
            playerViewModel.minimizePlayer()
        }
    }


    if (albums.isEmpty()) {

        // --- Enhanced empty state (exactly like Videos tab) ---
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Different message based on whether we have any albums at all
                    // (you can adapt the condition – here we assume the library's total album count is known)
                    val totalAlbums = viewModel.uiState.value.albums.size
                    Text(
                        text = if (totalAlbums == 0)
                            "No albums in library"
                        else
                            "No albums found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (totalAlbums == 0)
                            "Try refreshing data"
                        else
                            "Try changing your filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (totalAlbums == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Data")
                        }
                    }
                }
            }
        }
        return
    }

    if (albums.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No albums in this filter")
        }
        return
    }


    val album = albums.find { "album_${it.albumId}" == playerUiState.activeCardId } ?: albums.first()
    val coroutineScope = rememberCoroutineScope()




    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        item {
            Log.d("SingleArtistView", "AlbumsSection with size=${albums.size}")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                AlbumsSection(
                    onAlbumSelected = { album ->
                        val cardId = "album_${album.albumId}"
                        if (playerUiState.activeCardId == cardId) {
                            // Close the player
                            playerViewModel.closePlayer(currentFilterPathId)
                        } else {
                            coroutineScope.launch {
                                val songs =
                                    viewModel.loadAlbumSongsCached(album.albumId)
                                if (songs.isNotEmpty()) {
                                    val firstSong = songs.first()
                                    val playlist =
                                        songs.map {
                                            PlaylistItem.SongItem(
                                                it,
                                                album.albumId
                                            )
                                        }
                                    firstSong.ytVideoId?.let { videoId ->
                                        playerViewModel.loadVideo(
                                            videoId = videoId,
                                            cardId = "album_${album.albumId}",
                                            currentFilterPath = filterPath,
                                            startInMiniMode = true,
                                            mediaDbId = firstSong.songId,
                                            filterPathId = currentFilterPathId,
                                            typeOfMedia = 1,
                                            playlist = playlist,
                                            startIndex = 0
                                        )
                                    }
                                }
                            }
                        }
                    },
                    currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
                    albumsDisplay = albums,
                    currentFilterPathId = currentFilterPathId,
                    minimiseMaximiseToggle = minimiseMaximiseToggle,
                    showMainAndFeaturedChips = true,
                    albumArtistsMap = albumArtistsMap,
                    activeCardId = playerUiState.activeCardId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        item(key = "album_${album.albumId}") {
            Log.d(
                "AlbumsListContent",
                "🃏 Building card for album_${album.albumId}, activeCardId = ${playerUiState.activeCardId}"
            )

            var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
            LaunchedEffect(album.albumId) {
                songs = viewModel.loadAlbumSongsCached(album.albumId)
                Log.d(
                    "AlbumsListContent",
                    "Loaded ${songs.size} songs for album ${album.title}"
                )
            }

            val thumbnailUrl = album.getThumbnailUrl()
            val firstSong = songs.firstOrNull()
            val youtubeVideoId = firstSong?.ytVideoId
            val albumArtists =
                albumArtistsMap[album.albumId]?.map { it.artist } ?: emptyList()


            AlbumPlayerCard(
                album = album,
                songs = songs,
                isActive = playerUiState.activeCardId == "album_${album.albumId}",
                isPlayerVisible = isPlayerVisible,
                onAlbumClick = {
                    Log.d(
                        "AlbumsListContent",
                        "🎯 Album thumbnail clicked: ${album.title} (album_${album.albumId})"
                    )
                    firstSong?.ytVideoId?.let { videoId ->
                        Log.d(
                            "AlbumsListContent",
                            "▶️ Playing first song, cardId = album_${album.albumId}"
                        )
                        val playlist =
                            songs.map { PlaylistItem.SongItem(it, album.albumId) }
                        playerViewModel.loadVideo(
                            videoId = videoId,
                            cardId = "album_${album.albumId}",
                            currentFilterPath = null,
                            startInMiniMode = false,   // ❗ starts attached (full mode)
                            mediaDbId = firstSong.songId,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 1,
                            playlist = playlist,
                            startIndex = 0
                        )
                    } ?: Log.w(
                        "AlbumsListContent",
                        "❌ No first song found for album ${album.title}"
                    )
                },
                onSongClick = { song ->
                    Log.d(
                        "AlbumsListContent",
                        "🎵 Song clicked: ${song.songTitle}, album=${album.title}"
                    )
                    val playlist =
                        songs.map { PlaylistItem.SongItem(it, album.albumId) }
                    val startIndex = songs.indexOfFirst { it.songId == song.songId }
                    song.ytVideoId?.let { videoId ->
                        playerViewModel.loadVideo(
                            videoId = videoId,
                            cardId = "album_${album.albumId}",
                            currentFilterPath = null,
                            startInMiniMode = false,
                            mediaDbId = song.songId,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 1,
                            playlist = playlist,
                            startIndex = startIndex
                        )
                    }
                },
                onActiveCardBoundsChanged = onActiveCardBoundsChanged,
                thumbnailUrl = thumbnailUrl,
                youtubeVideoId = youtubeVideoId,
                artists = albumArtists,
                onArtistClick = { artist ->
                    val alreadyFiltered = filterPath.any {
                        it.categoryId == FilterPath.CATEGORY_ARTIST && it.entityId == artist.id
                    }
                    if (!alreadyFiltered && artist.id != null) {
                        viewModel.handleChipSelection(
                            FilterPath.CATEGORY_ARTIST,
                            artist.id,
                            artist.fullName ?: "Unknown Artist",
                            true
                        )
                    }
                },
                currentPlayingSongId = currentPlayingSongId
            )
        }



    }

    // ---- VISIBILITY MONITOR (will log everything) ----
    PlayerCardVisibilityMonitor(
        listState = listState,
        activeCardId = playerUiState.activeCardId,
        playerViewModel = playerViewModel
    )

    // ---- extra log to confirm monitor is called ----
    LaunchedEffect(Unit) {
        Log.d("AlbumsListContent", "✅ AlbumsListContent composed, monitor attached")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumPlayerCard(
    album: Album,
    songs: List<Song>,
    isActive: Boolean,
    isPlayerVisible: Boolean,
    onAlbumClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    thumbnailUrl: String?,
    youtubeVideoId: String?,
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    currentPlayingSongId: Int?,
    modifier: Modifier = Modifier
) {
    val hasValidVideo = youtubeVideoId != null
    val scrollLockState = LocalScrollLock.current   // from composition

    // ─── Horizontal Pager ─────────────────────────────────────────
    val pageCount = 2   // 0: song list, 1: dummy (can be extended)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount }
    )
    val coroutineScope = rememberCoroutineScope()

    // ─── Nested scroll connection (consumes when locked) ──────────
    val nestedScrollConnection = remember(scrollLockState) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (scrollLockState.isLocked) {
                    // Swallow any remaining vertical scroll
                    Offset(0f, available.y)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            // --- Title row (same as before) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.title + " • " + album.year,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Thumbnail / video area (unchanged) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = hasValidVideo) {
                        if (hasValidVideo) onAlbumClick()
                    }
                    .then(
                        if (isActive && hasValidVideo) {
                            Modifier.onPlaced { coordinates ->
                                onActiveCardBoundsChanged(
                                    "album_${album.albumId}",
                                    IntOffset(
                                        x = coordinates.positionInRoot().x.roundToInt(),
                                        y = coordinates.positionInRoot().y.roundToInt()
                                    ),
                                    IntSize(coordinates.size.width, coordinates.size.height)
                                )
                            }
                        } else Modifier
                    )
            ) {
                // thumbnail & play overlay (unchanged)
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_error)
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = "Album cover",
                        modifier = Modifier.align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasValidVideo) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- Artist chips (unchanged) ---
            if (artists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    artists.forEach { artist ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onArtistClick(artist) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                ArtistImage(artist, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(artist.fullName, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // ─── NEW: PAGER AREA (replaces the old fixed song list) ───
                Spacer(modifier = Modifier.height(12.dp))

                // Fixed height container for the pager
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .nestedScroll(nestedScrollConnection)   // 👈 consumes scroll when locked
                ) {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> SongListPage(
                                songs = songs,
                                currentPlayingSongId = currentPlayingSongId,
                                onSongClick = onSongClick,
                                scrollLockState = scrollLockState
                            )

                            else -> DummyPageContent(page = page)
                        }
                    }
                }


                // Dots row for pager navigation (same as in WikiInfoCard)
                DotsRow(
                    pageCount = pageCount,
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

@Composable
private fun SongListPage(
    songs: List<Song>,
    currentPlayingSongId: Int?,
    onSongClick: (Song) -> Unit,
    scrollLockState: ScrollLockState
) {
    val coroutineScope = rememberCoroutineScope()
    var unlockJob by remember { mutableStateOf<Job?>(null) }

    val scrollState = rememberScrollState()
    val noFlingBehavior = remember {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                Log.d("SongListPage", "performFling called with velocity=$initialVelocity, isLocked=${scrollLockState.isLocked}")
                return 0f
            }
        }
    }

    val nestedScrollConnection = remember(scrollLockState) {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                Log.d("SongListPage", "onPreFling: available=$available, isLocked=${scrollLockState.isLocked}")
                return if (scrollLockState.isLocked) {
                    Log.d("SongListPage", "✅ Consuming whole fling velocity")
                    available   // consume everything
                } else {
                    Log.d("SongListPage", "❌ NOT consuming fling (lock false)")
                    Velocity.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                Log.d("SongListPage", "onPostScroll: available=$available, source=$source, isLocked=${scrollLockState.isLocked}")
                return if (scrollLockState.isLocked) {
                    Log.d("SongListPage", "✅ Consuming remaining scroll $available")
                    Offset(0f, available.y)
                } else {
                    Offset.Zero
                }
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                Log.d("SongListPage", "onPreScroll: available=$available, source=$source, isLocked=${scrollLockState.isLocked}")
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState) //, flingBehavior = noFlingBehavior
            .pointerInput(Unit) {
                awaitEachGesture {
                    var unlockJob: Job? = null
                    try {
                        awaitFirstDown(requireUnconsumed = false)
                        scrollLockState.isLocked = true
                        // Wait for touch release
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Release || event.type == PointerEventType.Exit) {
                                unlockJob = coroutineScope.launch {
                                    delay(100)
                                    scrollLockState.isLocked = false
                                }
                                break
                            }
                        }
                    } catch (e: CancellationException) {
                        unlockJob?.cancel()
                        scrollLockState.isLocked = false
                        throw e
                    }
                }
            }
    ) {
        songs.forEachIndexed { index, song ->
            SongRow(
                song = song,
                isCurrentlyPlaying = song.songId == currentPlayingSongId,
                onClick = { onSongClick(song) }
            )
            if (index != songs.lastIndex) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .background(
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
                else Color.Transparent
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = song.songTitle?: "N/A",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AlbumsSection(
    onAlbumSelected: (Album) -> Unit,
    currentMediaEntryTypeOfMedia: Int?,
    albumsDisplay: List<Album>,
    currentFilterPathId: Int?,
    albumArtistsMap: Map<Int, List<MainViewModel.AlbumArtistInfo>>,
    minimiseMaximiseToggle: Boolean,
    showMainAndFeaturedChips: Boolean,
    activeCardId: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {


        AlbumGridView(
            onAlbumClick = onAlbumSelected,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            activeCardId = activeCardId,
            albumArtistsMap = albumArtistsMap,
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
    albumArtistsMap: Map<Int, List<MainViewModel.AlbumArtistInfo>>,
    showMainAndFeaturedChips: Boolean,
    activeCardId: String?,
    modifier: Modifier = Modifier
) {

    Log.d("Recomposition", "AlbumGrid recomposed at ${System.currentTimeMillis()}")

    // Shuffled vs alphabetical mode (only active when no year/rating sort)
    var isAlphabeticalMode by remember { mutableStateOf(false) }

    // ✅ ADD THIS: Wait for artist map to be ready (if there are albums)
    val isMapReady = albumsDisplay.isEmpty() || albumArtistsMap.isNotEmpty()
    if (!isMapReady) {
        // Show a loading spinner while waiting
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }


    fun isMainAlbum(album: Album): Boolean {
        return albumArtistsMap[album.albumId]
            ?.any { if (album.isMain == 1) true else false } == true
    }

    // Helper: get display artist name (first artist's full name)
    fun getArtistDisplayName(album: Album): String {
        return albumArtistsMap[album.albumId]
            ?.firstOrNull()
            ?.artist
            ?.fullName
            ?: ""
    }

    // Tab state
    val mainCount = remember(albumsDisplay, albumArtistsMap) {
        albumsDisplay.count { isMainAlbum(it) }
    }
    val featuredCount = remember(albumsDisplay, albumArtistsMap) {
        albumsDisplay.count { !isMainAlbum(it) }
    }

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

    val filteredByTab = remember(albumsDisplay, selectedTab, albumArtistsMap) {
        when (selectedTab) {
            AlbumGridTab.MAIN -> albumsDisplay.filter { isMainAlbum(it) }
            AlbumGridTab.FEATURED -> albumsDisplay.filter { !isMainAlbum(it) }
            null -> emptyList()
        }
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
                val comparator =
                    if (ratingSort == SortDirection.ASC) combined else combined.reversed()
                filteredByTab.sortedWith(comparator)
            }

            yearSort != null -> {
                val baseComparator = compareBy<Album, Int?>(nullsLast()) { it.year }
                val comparator =
                    if (yearSort == SortDirection.ASC) baseComparator else baseComparator.reversed()
                filteredByTab.sortedWith(comparator)
            }

            else -> {
                if (isAlphabeticalMode) {
                    filteredByTab.sortedWith(compareBy { it.title?.lowercase() ?: "" })
                } else {
                    filteredByTab
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
            Log.d(
                TAG,
                "Note updated: mode=${if (ratingSort != null) "rating" else if (yearSort != null) "year" else "alpha"}, text=$currentNoteText, album=${album.title}"
            )
        } else {
            currentNoteText = ""
        }
    }

    // ========== GRID SCROLL STATE ==========
    val gridState = remember(currentFilterPathId, albumsDisplay.size) {
        Log.d(
            TAG,
            "🔄 Creating NEW LazyGridState! filterPathId=$currentFilterPathId, size=${albumsDisplay.size}"
        )
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
        (sortedAlbums.size + (albumsPerDot - 1)) / albumsPerDot  // ceil division
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
        Log.d(
            TAG,
            "scrollToItemIndex requested: index=$index, sortedAlbums.size=${sortedAlbums.size}"
        )
        if (index in sortedAlbums.indices) {
            coroutineScope.launch {
                Log.d(TAG, "Executing scrollToItem($index)")
                gridState.scrollToItem(index)
                delay(100)
                Log.d(TAG, "After scroll: firstVisibleItemIndex=${gridState.firstVisibleItemIndex}")
            }
        } else {
            Log.w(
                TAG,
                "scrollToItemIndex: index $index out of bounds (0..${sortedAlbums.lastIndex})"
            )
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
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            if (sortedAlbums.isEmpty()) {

                Text(
                    text = "No albums to display",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )


            } else {
                val fixedGridCells = if (minimiseMaximiseToggle) 2 else 1

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(fixedGridCells),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    val cardWidth = if (minimiseMaximiseToggle) 120.dp else 250.dp

                    items(sortedAlbums) { album ->
                        val isActive = activeCardId == "album_${album.albumId}"
                        Log.d(
                            "AlbumGridView",
                            "🖼️ Rendering album card: ${album.title} (id=${album.albumId})"
                        )
                        AlbumCard(
                            album = album,
                            artistName = getArtistDisplayName(album),  // ← computed from map
                            modifier = Modifier
                                .width(cardWidth)
                                .animateContentSize(),
                            onClick = { onAlbumClick(album) },
                            isActive = isActive
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
fun AlbumCard(
    album: Album,
    artistName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    val thumbnailUrl = album.getThumbnailUrl()
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isActive) 1.dp else 0.dp

    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = album.released?.take(4) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )

                Text(
                    text = artistName,   // ← now from map, not from album.artistFullName
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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
                        val dotIndex = (position.x / dotWidth)
                            .toInt()
                            .coerceIn(0, dotCount - 1)
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
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


@Composable
private fun DummyPageContent(page: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Page $page – Dummy Content",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}