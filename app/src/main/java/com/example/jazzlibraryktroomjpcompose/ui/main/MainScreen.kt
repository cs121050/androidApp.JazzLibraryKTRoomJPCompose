package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val rightDrawerState by viewModel.rightDrawerState.collectAsState()
    val leftDrawerState by viewModel.leftDrawerState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    // NEW: Snackbar states
    val showError by viewModel.showError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Animate drawer offsets
    val rightDrawerOffset by animateDpAsState(
        targetValue = if (rightDrawerState == DrawerState.OPEN) 0.dp else 320.dp
    )

    val leftDrawerOffset by animateDpAsState(
        targetValue = if (leftDrawerState == DrawerState.OPEN) 0.dp else (-320).dp
    )

// Show loading screen only during initial load
    if (loadingState == LoadingState.LOADING && uiState.videos.isEmpty()) {
        LoadingScreen()
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main Content
            MainContent(
                uiState = uiState,
                filterState = filterState,
                viewModel = viewModel,
                onMenuClick = { viewModel.toggleLeftDrawer() },
                onFilterClick = { viewModel.toggleRightDrawer() },
                onClearFilters = { viewModel.clearAllFilters() },
                onRefresh = { viewModel.safeRefreshData() },
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = leftDrawerOffset + rightDrawerOffset)
            )

            // Left Drawer
            LeftDrawer(
                isOpen = leftDrawerState == DrawerState.OPEN,
                onClose = { viewModel.toggleLeftDrawer() },
                onRefreshClick = { viewModel.safeRefreshData() },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .offset(x = leftDrawerOffset)
            )

            // Right Drawer
            RightDrawer(
                uiState = uiState,
                filterState = filterState,
                isOpen = rightDrawerState == DrawerState.OPEN,
                onChipSelected = { categoryId, entityId, entityName, isSelected ->
                    viewModel.handleChipSelection(categoryId, entityId, entityName, isSelected)
                },
                onClose = { viewModel.toggleRightDrawer() },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .offset(x = rightDrawerOffset)
            )

            // NEW: Snackbar for errors (non-blocking)
            if (showError && errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                        modifier = Modifier.padding(8.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
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
    onRefresh: () -> Unit, // NEW: Refresh callback
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Jazz.li",
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
                // Refresh button
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                }
                // Filter button
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Path Chips (Active Filters)
        if (filterState.currentFilterPath.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterState.currentFilterPath.forEach { filter ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            // Now we can access viewModel
                            viewModel.handleChipSelection(
                                filter.categoryId,
                                filter.entityId,
                                filter.entityName,
                                false
                            )
                        },
                        label = {
                            Text(filter.entityName)
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    )
                }

                // Clear All Button
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Bar
        var searchText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search videos, artists...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Data Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${uiState.videos.size} Videos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${uiState.allInstruments.size} Instruments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Videos List
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
            val videosToShow = if (filterState.currentFilterPath.isEmpty()) {
                uiState.videos
            } else {
                uiState.filteredVideos
            }

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
                            text = if (uiState.videos.isEmpty()) "No videos in library" else "No videos found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.videos.isEmpty()) "Try refreshing data" else "Try changing your filters",
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
                    item {
                        Text(
                            text = "Videos (${videosToShow.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(videosToShow) { video ->
                        VideoCard(video = video)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCard(
    video: com.example.jazzlibraryktroomjpcompose.domain.models.Video
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle video click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = video.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = video.path ?: "No location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pin button
                IconButton(
                    onClick = { /* Handle pin/unpin */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList, // Placeholder - change to pin icon
                        contentDescription = "Pin video",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Placeholder for YouTubePlayerView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "YouTube Player",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = video.duration,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: ${video.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "Available: ${video.availability}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}