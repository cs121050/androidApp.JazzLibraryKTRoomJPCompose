package com.example.jazzlibraryktroomjpcompose.ui.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.layout.positionInParent

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val leftDrawerState by viewModel.leftDrawerState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    // NEW: Snackbar states
    val showError by viewModel.showError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    // --- State for player visibility (global toggle) ---
    var isPlayerVisible by remember { mutableStateOf(true) }

    val leftDrawerOffset by animateDpAsState(
        targetValue = if (leftDrawerState == DrawerState.OPEN) 0.dp else (-320).dp
    )

    val bottomSheetState by viewModel.bottomSheetState.collectAsState()
    val context = LocalContext.current
    // Double back press state
    var backPressTime by remember { mutableLongStateOf(0L) }

    // Single BackHandler that handles both cases
    BackHandler(
        enabled = true,
        onBack = {
            // If bottom sheet is open, close it
            if (bottomSheetState != BottomSheetState.HIDDEN) {
                viewModel.setBottomSheetState(BottomSheetState.HIDDEN)
                return@BackHandler
            }

            // Otherwise handle double back exit
            val currentTime = System.currentTimeMillis()

            if (currentTime - backPressTime > 500) {
                // First press - show toast
//                android.widget.Toast.makeText(
//                    context,
//                    "Press back again to exit",
//                    android.widget.Toast.LENGTH_SHORT
//                ).show()
                backPressTime = currentTime
            } else {
                // Second press within 500ms seconds - exit
                (context as? android.app.Activity)?.finish()
            }
        }
    )

    // Show loading screen only during initial load
    if (loadingState == LoadingState.LOADING && uiState.videos.isEmpty()) {
        LoadingScreen()
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // MAIN CONTENT
            // --- MAIN CONTENT (with global player toggle passed) ---
            MainContent(
                uiState = uiState,
                filterState = filterState,
                viewModel = viewModel,
                onMenuClick = { viewModel.toggleLeftDrawer() },
                onFilterClick = { viewModel.toggleBottomSheet() },
                onClearFilters = { viewModel.clearAllFilters() },
                onRefresh = { viewModel.safeRefreshData() },
                // NEW: global player visibility + toggle callback
                isPlayerVisible = isPlayerVisible,
                onTogglePlayerVisibility = { isPlayerVisible = !isPlayerVisible },
                modifier = Modifier.fillMaxSize()
            )

            // LEFT DRAWER
            LeftDrawer(
                isOpen = leftDrawerState == DrawerState.OPEN,
                onClose = { viewModel.toggleLeftDrawer() },
                onRefreshClick = { viewModel.safeRefreshData() },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .offset(x = leftDrawerOffset)
            )

            // With this:
            // FILTER BOTTOM SHEET - YouTube-like behavior
            YouTubeLikeBottomSheet(
                viewModel = viewModel,
                uiState = uiState,
                filterState = filterState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )

            // SNACKBAR (will appear above everything)
            if (showError && errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.largePadding),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        action = {
                            TextButton(
                                onClick = { viewModel.dismissError() }
                            ) {
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
    onMenuClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    isPlayerVisible: Boolean,
    onTogglePlayerVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
//        // --- Top Bar ---
//        TopAppBar(
//            title = {
//                Text(
//                    text = "Jazzli",
//                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.primary
//                )
//            },
//            navigationIcon = {
//                IconButton(onClick = onMenuClick) {
//                    Icon(Icons.Default.Menu, contentDescription = "Menu")
//                }
//            },
//            actions = {
//                IconButton(onClick = onRefresh) {
//                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
//                }
//                IconButton(onClick = onFilterClick) {
//                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
//                }
//            }
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))


        // --- Active Filter Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clickable Logo Text
            Text(
                text = "Jazzli",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold
                //fontSize = 32.sp  // Increase this value as needed
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onMenuClick() }
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (filterState.currentFilterPath.isNotEmpty()) {
                    filterState.currentFilterPath.forEach { filter ->
                        FilterPathChip(
                            text = filter.entityName,
                            isSelected = false, //TODO// customisable: true : to make the filter path chips Blue, like got selected at bottom sheet
                            onClick = {
                                viewModel.handleChipSelection(
                                    filter.categoryId,
                                    filter.entityId,
                                    filter.entityName,
                                    false
                                )
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))


        // --- Search Bar ---
        var searchText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(16.dp))

        // --- Data Stats Row with List Toggle Icon ---
        val videosToShow = if (filterState.currentFilterPath.isEmpty()) {
            uiState.videos
        } else {
            uiState.filteredVideos
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Videos (${videosToShow.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // 🔁 NEW: IconToggleButton for global player visibility
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
                        "hide players"
                    else
                        "Show players",
                    tint = if (isPlayerVisible)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Videos List ---
        if (filterState.isFiltering) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize(),
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videosToShow) { video ->
                        // 🔁 Pass only isPlayerVisible – no per‑card toggle callback
                        VideoCard(
                            video = video,
                            isPlayerVisible = isPlayerVisible
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCard(
    video: Video,
    isPlayerVisible: Boolean,
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
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            Icons.Default.PlayArrow,
//                            contentDescription = "Duration",
//                            modifier = Modifier.size(16.dp),
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = video.duration ?: "Unknown",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
                }

//                Icon(
//                    imageVector = if (expanded || isPlayerVisible)
//                        Icons.Default.ExpandLess
//                    else
//                        Icons.Default.ExpandMore,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(20.dp)
//                )
            }

            // --- YouTube Player (collapsible) ---
            AnimatedVisibility(
                visible = isPlayerVisible || expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)),
                exit  = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (videoId != null) {
                            YoutubeVideoPlayer(
                                videoId = videoId,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Invalid video URL",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // --- Fullscreen button (opens YouTube app) ---
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
