package com.example.jazzlibraryktroomjpcompose.ui.common.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlaylistItem
import com.example.jazzlibraryktroomjpcompose.ui.common.util.extractYouTubeVideoId
import com.example.jazzlibraryktroomjpcompose.ui.main.MainTab
import com.example.jazzlibraryktroomjpcompose.ui.main.MainViewModel
import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens
import kotlinx.coroutines.launch


@Composable
fun TabText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    count: Int
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
fun VideoStatsRow(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onTabSelected: (MainTab) -> Unit,
    onVideoTabClick: () -> Unit,
    onAlbumTabClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()
    val currentFilterPath by viewModel.currentFilterPath.collectAsState()
    val currentFilterPathId by viewModel.currentFilterPathId.collectAsState()
    val currentAlbumId by viewModel.currentAlbumId.collectAsState()
    val currentAlbumSongs by viewModel.albumSongs.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val playerSession by playerViewModel.playerSession.collectAsState()
    val isActiveCardVisible by playerViewModel.isActiveCardVisible.collectAsState()
    val enrichedHistory by viewModel.enrichedHistory.collectAsState()

    val videoCount = if (filterState.currentFilterPath.isEmpty()) uiState.videos.size else uiState.filteredVideos.size
    val artistCount = uiState.availableArtists.size
    val albumCount = uiState.filteredAlbums.size
    val historyCount = enrichedHistory.size

    val videos = if (filterState.currentFilterPath.isEmpty())
        uiState.videos else uiState.filteredVideos

    val canGoPrev = playerSession?.let { it.currentIndex > 0 } ?: false
    val canGoNext = playerSession?.let { it.currentIndex < it.playlist.size - 1 } ?: false
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
                    else -> !isVideoIndexVisible(0)   // index 0 for first video
                }
                val playlist = videos.map { PlaylistItem.VideoItem(it) }
                val startIndex = 0
                playerViewModel.loadVideo(
                    videoId = videoId,
                    cardId = firstVideo.locationId,
                    currentFilterPath = currentFilterPath,
                    startInMiniMode = startInMiniMode,
                    mediaDbId = firstVideo.id,
                    filterPathId = currentFilterPathId,
                    typeOfMedia = 0,
                    playlist = playlist,
                    startIndex = startIndex
                )
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            1 -> {
                // Only auto‑start if nothing is currently playing
                val nothingPlaying = !playerUiState.isVisible
                if (!nothingPlaying) return@LaunchedEffect

                when (currentTab) {
                    MainTab.VIDEOS -> {
                        if (videos.isNotEmpty()) {
                            loadFirstVideo()
                        }
                    }
                    MainTab.ALBUMS -> {
                        val albumId = currentAlbumId
                        val songs = currentAlbumSongs
                        if (albumId != null && songs.isNotEmpty()) {
                            val firstSong = songs.first()
                            val playlist = songs.map { PlaylistItem.SongItem(it, albumId) }
                            val startIndex = 0
                            playerViewModel.loadVideo(
                                videoId = firstSong.ytVideoId!!,
                                cardId = "album_$albumId",
                                currentFilterPath = currentFilterPath,
                                startInMiniMode = false,
                                mediaDbId = firstSong.songId,
                                filterPathId = currentFilterPathId,
                                typeOfMedia = 1,
                                playlist = playlist,
                                startIndex = startIndex
                            )
                        }
                    }

                    else -> { /* History tab – do nothing */ }
                }
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
                text = "Videos",
                selected = currentTab == MainTab.VIDEOS,
                onClick = {
                    onTabSelected(MainTab.VIDEOS)
                    if (currentTab == MainTab.VIDEOS) {  // already on Videos tab
                        onVideoTabClick()
                    }
                },
                count = videoCount
            )
            TabText(
                text = "Albums",
                selected = currentTab == MainTab.ALBUMS,
                onClick = {
                    onTabSelected(MainTab.ALBUMS)
                    if (currentTab == MainTab.ALBUMS) {
                        onAlbumTabClick()
                    }
                },
                count = albumCount   // placeholder count, you can replace later
            )
            TabText(
                text = "Artists",
                selected = currentTab == MainTab.ARTISTS,
                onClick = { onTabSelected(MainTab.ARTISTS) },
                count = artistCount
            )
            TabText(
                text = "History",
                selected = currentTab == MainTab.HISTORY,
                onClick = { onTabSelected(MainTab.HISTORY) },
                count = historyCount
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
                            Toast.makeText(context, "Share - to be implemented", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Cast - to be implemented", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(Icons.Default.Cast, contentDescription = "Cast")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Back - to be implemented", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }

                1 -> {
                    val currentType = playerUiState.currentTypeOfMedia
                    val isAlbumMode = currentType == 1

                    // Determine if previous/next are possible (reuse your existing logic)


                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        // Previous button
                        IconButton(
                            onClick = {
                                val shouldStartInMini = when {
                                    currentTab != MainTab.ARTISTS -> true
                                    else -> !isActiveCardVisible   // visibility of the active card (original album)
                                }
                                playerViewModel.previousVideo(shouldStartInMini)
                            },
                            enabled = canGoPrev
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }

                        // Next button
                        IconButton(
                            onClick = {
                                val shouldStartInMini = when {
                                    currentTab != MainTab.ARTISTS -> true
                                    else -> !isActiveCardVisible   // visibility of the active card (original album)
                                }

                                playerViewModel.nextVideo(shouldStartInMini)
                                // and similarly for previous
                            },
                            enabled = canGoNext
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        }

                        // Close button (unchanged)
                        IconButton(onClick = {
                            coroutineScope.launch {
                                playerViewModel.closePlayer(viewModel.currentFilterPathId.value)
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
                            onCheckedChange = { viewModel.toggleBottomSheet() }
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
