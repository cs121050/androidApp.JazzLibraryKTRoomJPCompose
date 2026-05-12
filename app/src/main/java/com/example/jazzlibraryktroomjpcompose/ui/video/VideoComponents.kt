package com.example.jazzlibraryktroomjpcompose.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jazzlibraryktroomjpcompose.R
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerStableState
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlaylistItem
import com.example.jazzlibraryktroomjpcompose.ui.artist.ArtistImage
import com.example.jazzlibraryktroomjpcompose.ui.common.player.PlayerCardVisibilityMonitor
import com.example.jazzlibraryktroomjpcompose.ui.common.util.extractYouTubeVideoId
import com.example.jazzlibraryktroomjpcompose.ui.main.CardUiState
import com.example.jazzlibraryktroomjpcompose.ui.main.FilterState
import com.example.jazzlibraryktroomjpcompose.ui.main.MainTab
import com.example.jazzlibraryktroomjpcompose.ui.main.MainUiState
import com.example.jazzlibraryktroomjpcompose.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun VideoListContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),

    onActiveCardBoundsChanged: (String, IntOffset, IntSize) -> Unit,
    onCardTitleClick: (String) -> Unit,
    onRefresh: () -> Unit
    ) {

    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val playerUiState by playerViewModel.stableState.collectAsState()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()
    val videoArtistsMap by viewModel.videoArtistsMap.collectAsState()
    val currentFilterPathId by viewModel.currentFilterPathId.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val videosToShow = if (filterState.currentFilterPath.isEmpty())
        uiState.videos else uiState.filteredVideos

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
                val preloadRange =
                    (firstVisible + 1)..(firstVisible + 5).coerceAtMost(videosToShow.lastIndex)

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
            }
        } else {

            LazyColumn(
                state = listState,
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = videosToShow,
                    key = { it.locationId }
                ) { video ->
                    val clickedVideoIndex = videosToShow.indexOfFirst { it.id == video.id }
                    val startInMiniMode = if (currentTab != MainTab.VIDEOS) {
                        true
                    } else {
                        val isCardVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == clickedVideoIndex }
                        !isCardVisible
                    }

                    VideoCard(
                        video = video,
                        isActive = video.locationId == playerUiState.activeCardId,
                        isPlayerVisible = isPlayerVisible,
                        onCardClick = {
                            val videoId = extractYouTubeVideoId(video.path)
                            if (videoId != null) {
                                val playlist = videosToShow.map { PlaylistItem.VideoItem(it) }
                                val startIndex = videosToShow.indexOfFirst { it.id == video.id }
                                playerViewModel.loadVideo(
                                    videoId = videoId,
                                    cardId = video.locationId,
                                    currentFilterPath = filterState.currentFilterPath,
                                    startInMiniMode = startInMiniMode,
                                    mediaDbId = video.id,
                                    filterPathId = currentFilterPathId,
                                    typeOfMedia = 0,
                                    playlist = playlist,
                                    startIndex = startIndex
                                )
                            }
                        },
                        onCardTitleClick = { onCardTitleClick(video.locationId) },
                        artists = videoArtistsMap[video.id] ?: emptyList(),
                        onArtistClick = { artist ->
                            val alreadyFiltered = filterState.currentFilterPath.any {
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
                        onActiveCardBoundsChanged = { cardId, position, size ->
                            onActiveCardBoundsChanged(cardId, position, size)
                        }
                    )
                }

                // Add spacer at bottom
                item {
                    if(videosToShow.isNotEmpty())
                        Spacer(modifier = Modifier.height(220.dp))
                }
            }

            PlayerCardVisibilityMonitor(
                listState = listState,
                activeCardId = playerUiState.activeCardId,
                playerViewModel = playerViewModel
            )

        }
    }
}

@Composable
fun VideoCard(
    video: Video,
    isActive: Boolean = false,
    isPlayerVisible: Boolean = true,
    onCardClick: () -> Unit,
    onCardTitleClick: () -> Unit = {},
    artists: List<Artist> = emptyList(),
    onArtistClick: (Artist) -> Unit = {},
    onActiveCardBoundsChanged: ((String, IntOffset, IntSize) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardTitleClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thumbnail (square or 16:9)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onCardClick() }
                    .then(
                        if (isActive && onActiveCardBoundsChanged != null) {
                            Modifier.onPlaced { coordinates ->
                                onActiveCardBoundsChanged(
                                    video.locationId,
                                    IntOffset(
                                        coordinates.positionInRoot().x.roundToInt(),
                                        coordinates.positionInRoot().y.roundToInt()
                                    ),
                                    IntSize(coordinates.size.width, coordinates.size.height)
                                )
                            }
                        } else Modifier
                    )
            ) {
                val thumbnailUrl = video.getThumbnailUrl()
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Video thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_error)
                    )
                }
                // Play overlay
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Artists row
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
        }
    }
}