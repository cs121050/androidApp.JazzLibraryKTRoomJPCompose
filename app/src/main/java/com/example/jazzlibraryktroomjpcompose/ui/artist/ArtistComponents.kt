package com.example.jazzlibraryktroomjpcompose.ui.artist

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jazzlibraryktroomjpcompose.R
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerStableState
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlaylistItem
import com.example.jazzlibraryktroomjpcompose.ui.album.AlbumsSection
import com.example.jazzlibraryktroomjpcompose.ui.common.components.DotsRow
import com.example.jazzlibraryktroomjpcompose.ui.common.components.FastScrollingDotsRow
import com.example.jazzlibraryktroomjpcompose.ui.common.components.LocalScrollLock
import com.example.jazzlibraryktroomjpcompose.ui.common.player.PlayerCardVisibilityMonitor
import com.example.jazzlibraryktroomjpcompose.ui.common.util.generateIdenticon
import com.example.jazzlibraryktroomjpcompose.ui.common.util.parseWikipediaData
import com.example.jazzlibraryktroomjpcompose.ui.main.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun ArtistContent(
    modifier: Modifier = Modifier,
    artistsShuffled: List<Artist>,
    artistsBase: List<Artist>,
    filteredAlbums: List<Album>,
    albumsDisplay: List<Album>,
    playerUiState: PlayerStableState,
    onRefresh: () -> Unit = {},
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    filterPath: List<FilterPath>, // new parameter
    onArtistSelected: (Artist) -> Unit = {},
    onAlbumSelected: (Album) -> Unit = {},
    scrollToAlbumsTrigger: MutableState<Int>,
    currentFilterPathId: Int?,
    minimiseMaximiseToggle: Boolean,
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    albumArtistsMap: Map<Int, List<MainViewModel.AlbumArtistInfo>>,
    currentMediaEntryTypeOfMedia: Int?
) {


    Log.d("ArtistContent", "🎨 ArtistContent recompose: filterPath=$filterPath")
    Log.d(
        "ArtistContent",
        "📀 albumsDisplay size=${albumsDisplay.size}, first 3 titles=${
            albumsDisplay.take(3).joinToString { it.title ?: "null" }
        }"
    )

    val listState = rememberLazyListState()     // lazy list state for scrolling


    // Check if there's an artist filter
    val selectedArtist = filterPath
        .firstOrNull { it.categoryId == FilterPath.CATEGORY_ARTIST }
        ?.let { filter ->
            // Find the artist in the base list (or shuffled) by ID
            artistsBase.find { it.id == filter.entityId }
        }

    // State to remember which album was clicked from the multi-artist grid
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    Log.d("ArtistContent", "selectedAlbum initialized as ${selectedAlbum?.title}")


    // Clear selected album when no artist filter is active (so you don't see old album later)
    LaunchedEffect(filterPath) {
        Log.d("ArtistContent", "🔍 filterPath changed: $filterPath")
        val artistFilter = filterPath.firstOrNull { it.categoryId == FilterPath.CATEGORY_ARTIST }
        if (artistFilter == null) {
            selectedAlbum = null
            viewModel.setCurrentAlbumId(null)  // Clear current album
        } else {
            // Only auto-select a default album if the user hasn't already picked one
            if (selectedAlbum == null) {
                val artist = artistsBase.find { it.id == artistFilter.entityId }
                if (artist != null) {
                    val artistAlbums = albumsDisplay.filter { album ->
                        albumArtistsMap[album.albumId]?.any { it.artist.id == artist.id } == true
                    }
                    if (artistAlbums.isNotEmpty()) {
                        val firstAlbum = artistAlbums.first()
                        selectedAlbum = firstAlbum
                        viewModel.setCurrentAlbumId(firstAlbum.albumId)
                    } else {
                        selectedAlbum = null
                        viewModel.setCurrentAlbumId(null)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedArtist) {
        if (selectedArtist == null) {
            // Exited single artist view, minimize player
            playerViewModel.minimizePlayer()
        }
    }

    // Wrap the original onAlbumSelected to also remember the clicked album
    val handleAlbumSelected: (Album) -> Unit = { album ->
        Log.d(
            "ArtistContent",
            "🖱️ handleAlbumSelected called with album: ${album.title} (id=${album.albumId})"
        )
        selectedAlbum = album
        onAlbumSelected(album)  // this adds the artist filter and triggers scroll
    }




    if (selectedArtist != null) {
        // Single artist view

        Log.d(
            "ArtistContent",
            "🎤 Single artist mode: ${selectedArtist.fullName}, passing initialSelectedAlbum=${selectedAlbum?.title}"
        )
        SingleArtistView(
            artist = selectedArtist,
            filteredAlbums = filteredAlbums,
            onAlbumSelected = onAlbumSelected,
            currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            minimiseMaximiseToggle = minimiseMaximiseToggle,
            scrollToAlbumsTrigger = scrollToAlbumsTrigger,
            viewModel = viewModel,
            albumArtistsMap = albumArtistsMap,
            playerViewModel = playerViewModel,
            currentFilterPath = filterPath,
            initialSelectedAlbum = selectedAlbum,
            playerUiState = playerUiState,
            onActiveCardBoundsChanged = onActiveCardBoundsChanged,
            modifier = modifier
        )
        Log.d(
            "ArtistContent",
            "Calling SingleArtistView with initialSelectedAlbum = ${selectedAlbum?.title}"
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
            currentMediaEntryTypeOfMedia = currentMediaEntryTypeOfMedia,
            albumsDisplay = albumsDisplay,
            currentFilterPathId = currentFilterPathId,
            minimiseMaximiseToggle = minimiseMaximiseToggle,
            scrollToAlbumsTrigger = scrollToAlbumsTrigger,
            viewModel = viewModel,
            albumArtistsMap = albumArtistsMap,
            playerViewModel = playerViewModel,
            currentFilterPath = filterPath,
            onActiveCardBoundsChanged = onActiveCardBoundsChanged,
            playerUiState = playerUiState,
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
        state = listState,
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
                                val rowArtists = pageArtists.slice(
                                    start until minOf(
                                        start + 2,
                                        pageArtists.size
                                    )
                                )
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

    PlayerCardVisibilityMonitor(
        listState = listState,   // you need to get the listState from the LazyColumn
        activeCardId = playerUiState.activeCardId,
        playerViewModel = playerViewModel
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
    currentFilterPath: List<FilterPath>,
    currentFilterPathId: Int?,
    minimiseMaximiseToggle: Boolean,
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    initialSelectedAlbum: Album? = null,
    albumArtistsMap: Map<Int, List<MainViewModel.AlbumArtistInfo>>,
    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    playerUiState: PlayerStableState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullscreenImage by remember { mutableStateOf(false) }

    // State used only for the grid – no player card
    var requestedAlbumId by remember { mutableStateOf(initialSelectedAlbum?.albumId) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var isDefaultSelection by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    // Watch for changes in albumsDisplay and requestedAlbumId
    LaunchedEffect(albumsDisplay, requestedAlbumId) {
        if (requestedAlbumId != null) {
            val found = albumsDisplay.find { it.albumId == requestedAlbumId }
            if (found != null && selectedAlbum?.albumId != requestedAlbumId) {
                selectedAlbum = found
                isDefaultSelection = false
                viewModel.setCurrentAlbumId(found.albumId)
            }
        }
        if (requestedAlbumId == null && selectedAlbum == null && albumsDisplay.isNotEmpty() && isDefaultSelection) {
            val first = albumsDisplay.first()
            selectedAlbum = first
            viewModel.setCurrentAlbumId(first.albumId)
        }
    }

    LaunchedEffect(initialSelectedAlbum) {
        if (initialSelectedAlbum != null && initialSelectedAlbum.albumId != requestedAlbumId) {
            requestedAlbumId = initialSelectedAlbum.albumId
            isDefaultSelection = false
            val existing = albumsDisplay.find { it.albumId == requestedAlbumId }
            if (existing != null) {
                selectedAlbum = existing
                viewModel.setCurrentAlbumId(existing.albumId)
            } else {
                selectedAlbum = null
            }
        }
    }

    val handleAlbumSelected: (Album) -> Unit = { album ->
        requestedAlbumId = album.albumId
        selectedAlbum = album
        isDefaultSelection = false
        viewModel.setCurrentAlbumId(album.albumId)
        onAlbumSelected(album)   // This should now navigate to album tab
    }

    val hasThumbnail = artist.thumbnailUrl != null
    val imageHeight = if (hasThumbnail) 300.dp else 150.dp

    val listState = rememberLazyListState()

    // Scroll to albums section (index 4) when triggered
    LaunchedEffect(scrollToAlbumsTrigger.value) {
        if (scrollToAlbumsTrigger.value > 0) {
            delay(100)
            listState.animateScrollToItem(4)   // albums section index
            scrollToAlbumsTrigger.value = 0
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Artist image
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val fallbackPainter = BitmapPainter(
                            generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap()
                        )
                        Image(
                            painter = fallbackPainter,
                            contentDescription = artist.fullName,
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }
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

        // 4. Albums section (grid)
        item {
            Log.d("SingleArtistView", "AlbumsSection with size=${albumsDisplay.size}")
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
                                val songs = viewModel.loadAlbumSongsCached(album.albumId)
                                if (songs.isNotEmpty()) {
                                    val firstSong = songs.first()
                                    val playlist =
                                        songs.map { PlaylistItem.SongItem(it, album.albumId) }
                                    firstSong.ytVideoId?.let { videoId ->
                                        playerViewModel.loadVideo(
                                            videoId = videoId,
                                            cardId = "album_${album.albumId}",
                                            currentFilterPath = currentFilterPath,
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
                    albumsDisplay = albumsDisplay,
                    currentFilterPathId = currentFilterPathId,
                    minimiseMaximiseToggle = minimiseMaximiseToggle,
                    showMainAndFeaturedChips = true,
                    albumArtistsMap = albumArtistsMap,
                    activeCardId = playerUiState.activeCardId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Add spacer at bottom
        item {
            if(albumsDisplay.isNotEmpty())
                Spacer(modifier = Modifier.height(220.dp))
        }

        // 5. No player card – removed
        // (All playback logic has been moved to AlbumsListContent)
    }

    // Fullscreen image dialog
    if (showFullscreenImage) {
        Dialog(
            onDismissRequest = { showFullscreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
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
                    val fallbackPainter = BitmapPainter(
                        generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap()
                    )
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
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
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

@Composable
fun ArtistImage(
    artist: Artist,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    // Generate on each composition – cheap enough
    val fallbackPainter =
        BitmapPainter(generateIdenticon(artist.fullName, artist.instrumentId).asImageBitmap())

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

    val scrollLockState = LocalScrollLock.current

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

    // Nested scroll connection that consumes BOTH scroll leftovers AND fling velocity
    val nestedScrollConnection = remember(scrollLockState) {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (scrollLockState.isLocked) {
                    Log.d("WikiInfoCard", "✅ Consuming fling $available")
                    available   // swallow entire fling
                } else {
                    Velocity.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (scrollLockState.isLocked) {
                    Log.d("WikiInfoCard", "✅ Consuming remaining scroll $available")
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
            .nestedScroll(nestedScrollConnection),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
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
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
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
        for (line in lines) {
            if (line.startsWith("### ")) {
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