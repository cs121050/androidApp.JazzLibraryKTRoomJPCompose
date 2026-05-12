package com.example.jazzlibraryktroomjpcompose.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerStableState
import com.example.jazzlibraryktroomjpcompose.presentation.player.PlayerViewModel
import com.example.jazzlibraryktroomjpcompose.ui.common.components.FilterPathChip
import com.example.jazzlibraryktroomjpcompose.ui.common.player.PlayerCardVisibilityMonitor
import com.example.jazzlibraryktroomjpcompose.ui.common.util.extractYouTubeVideoId
import com.example.jazzlibraryktroomjpcompose.ui.common.util.formatDate
import com.example.jazzlibraryktroomjpcompose.ui.common.util.getStartOfDay
import com.example.jazzlibraryktroomjpcompose.ui.main.MainViewModel
import com.example.jazzlibraryktroomjpcompose.ui.theme.Dimens


@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    viewModel: MainViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),

    onRefresh: () -> Unit
) {
    val enrichedHistory by viewModel.enrichedHistory.collectAsState()
    val playerUiState by playerViewModel.stableState.collectAsState()
    val currentPlayingDbId by playerViewModel.currentVideoDbIdState.collectAsState()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()

    // Load data when this screen appears
    LaunchedEffect(Unit) {
        viewModel.loadEnrichedHistory()
        playerViewModel.videoChangedEvent.collect {
            viewModel.loadEnrichedHistory()
        }
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
            state = listState,   // ADD THIS
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
            // Add spacer at bottom
            item {
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
                            .padding(
                                horizontal = Dimens.chiptextHorizontalPadding,
                                vertical = 6.dp
                            )
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
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 1f
                )
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