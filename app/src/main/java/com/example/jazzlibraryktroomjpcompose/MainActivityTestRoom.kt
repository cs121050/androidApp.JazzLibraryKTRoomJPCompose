package com.example.jazzlibraryktroomjpcompose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jazzlibraryktroomjpcompose.ui.DatabaseTestViewModel
import com.example.jazzlibraryktroomjpcompose.ui.theme.JazzLibraryKTRoomJPComposeTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyRow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityTestRoom : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JazzLibraryKTRoomJPComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DatabaseTestScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTestScreen(
    viewModel: DatabaseTestViewModel = viewModel()
) {
    val artists by viewModel.artists.collectAsState(initial = emptyList())
    val instruments by viewModel.instruments.collectAsState(initial = emptyList())
    val quotes by viewModel.quotes.collectAsState(initial = emptyList())
    val types by viewModel.types.collectAsState(initial = emptyList())
    val durations by viewModel.durations.collectAsState(initial = emptyList())
    val videos by viewModel.videos.collectAsState(initial = emptyList())
    val videoArtists by viewModel.videoArtists.collectAsState(initial = emptyList())

    val statusMessage by viewModel.statusMessage.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()

    // Create a scroll state for horizontal scrolling
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Jazz Library Database",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Data Source Indicator,
        //  this card includes 2 buttons to choose the sourse of the data
        //  that will populate the room DB
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (dataSource) {
                    DatabaseTestViewModel.DataSource.NONE -> MaterialTheme.colorScheme.surfaceVariant
                    DatabaseTestViewModel.DataSource.DUMMY -> MaterialTheme.colorScheme.secondaryContainer
                    DatabaseTestViewModel.DataSource.BOOTSTRAP -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Data Source: ${dataSource.name}",
                        style = MaterialTheme.typography.titleSmall,
                        color = when (dataSource) {
                            DatabaseTestViewModel.DataSource.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                            DatabaseTestViewModel.DataSource.DUMMY -> MaterialTheme.colorScheme.onSecondaryContainer
                            DatabaseTestViewModel.DataSource.BOOTSTRAP -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                    if (dataSource != DatabaseTestViewModel.DataSource.NONE) {
                        Text(
                            text = when (dataSource) {
                                DatabaseTestViewModel.DataSource.DUMMY -> "Using local test data"
                                DatabaseTestViewModel.DataSource.BOOTSTRAP -> "Using API data"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // Data Source Toggle Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = { viewModel.loadDummyData() },
                        label = { Text("Dummy Data") },
                        enabled = loadingState != DatabaseTestViewModel.LoadingState.Loading,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (dataSource == DatabaseTestViewModel.DataSource.DUMMY)
                                MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    AssistChip(
                        onClick = { viewModel.loadBootstrapData() },
                        label = { Text("Bootstrap API") },
                        enabled = loadingState != DatabaseTestViewModel.LoadingState.Loading,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (dataSource == DatabaseTestViewModel.DataSource.BOOTSTRAP)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        // Loading bar indicator
        when (loadingState) {
            DatabaseTestViewModel.LoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...")
                }
            }
            DatabaseTestViewModel.LoadingState.Error -> {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Error Loading Data",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            else -> {}
        }

        // Test Controls - Now in a horizontal scrollable container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Database Operations:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // First row of basic operations
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                    }
                ) {
                    Text("Clear All Data")
                }

                Button(
                    onClick = {
                        viewModel.refreshFromDb()
                    }
                ) {
                    Text("Refresh")
                }
            }

            Text(
                text = "Filter Testing:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )

            // Second row of filter operations
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
            ) {
                item{
                    Button(
                        onClick = {
                            viewModel.testAllFilteringQueries()
                        }
                    ) {
                        Text("Test Filters")
                    }
                }


                item{
                    Button(
                        onClick = {
                            viewModel.testFilterPathOperations()
                        }
                    ) {
                        Text("Test Filter Path")
                    }
                }

                item{
                    Button(
                    onClick = {
                        viewModel.testCompleteFilteringScenario()
                        }
                    ) {
                        Text("Test Scenario")
                    }
                }

                item{
                    Button(
                    onClick = {
                        viewModel.testAllCombinedFilterQueries()
                        }
                    ) {
                        Text("Test Combined")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.testAmbiguousColumnFix()
                        }
                    ) {
                        Text("Test Fix")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.testCompositionClasses()
                        }
                    ) {
                        Text("Test Composition")
                    }
                }
            }

            Text(
                text = "Filter System Tests:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )

            // Third row for filter system tests
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
            ) {
                item{
                    Button(
                        onClick = {
                            viewModel.testFilterPathScenarios()
                        }
                    ) {
                        Text("Test Filter Scenarios")
                    }
                }


                item{
                    Button(
                        onClick = {
                            viewModel.testChipGroupLogic()
                        }
                    ) {
                        Text("Test Chip Logic")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.testFilteredDataPopulation()
                        }
                    ) {
                        Text("Test Data Population")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.testAppStartupWithExistingFilters()
                        }
                    ) {
                        Text("Test App Startup")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.runAllFilterTests()
                        }
                    ) {
                        Text("Run All Filter Tests")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.testEdgeCases()
                        }
                    ) {
                        Text("Test Edge Cases")
                    }
                }

                item{
                    Button(
                        onClick = {
                            viewModel.clearAllFilters()
                        }
                    ) {
                        Text("Clear Filters")
                    }
                }
            }
        }

        // Status message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }

        // Tabs for different data types
        val tabCount = 8
        var selectedTab by remember { mutableStateOf(0) }

        // Create scrollable tab row for many tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Artists (${artists.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Instruments (${instruments.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Quotes (${quotes.size})") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Types (${types.size})") }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Durations (${durations.size})") }
            )
            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = { Text("Videos (${videos.size})") }
            )
            Tab(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                text = { Text("Video Artists (${videoArtists.size})") }
            )
            Tab(
                selected = selectedTab == 7,
                onClick = { selectedTab = 7 },
                text = {
                    val filteredData = viewModel.filteredData.collectAsState(initial = null).value
                    Text("Filtered Data (${filteredData?.videos?.size ?: 0})")
                }
            )
        }

        // Display data based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            when (selectedTab) {
                0 -> ArtistsList(artists = artists)
                1 -> InstrumentsList(instruments = instruments)
                2 -> QuotesList(quotes = quotes)
                3 -> TypesList(types = types)
                4 -> DurationsList(durations = durations)
                5 -> VideosList(videos = videos)
                6 -> VideoArtistsList(videoArtists = videoArtists)
                7 -> FilteredDataView(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FilteredDataView(viewModel: DatabaseTestViewModel) {
    val filteredData by viewModel.filteredData.collectAsState(initial = null)
    val filterPath by viewModel.filterPath.collectAsState(initial = emptyList())
    val filteringState by viewModel.filteringState.collectAsState(initial = DatabaseTestViewModel.FilteringState.IDLE)

    // Use a local variable to avoid recomposition issues
    val safeFilteredData = filteredData

    if (safeFilteredData == null) {
        Text(
            text = "No filtered data available. Apply filters first.",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Filters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (filterPath.isEmpty()) {
                        Text("No active filters")
                    } else {
                        filterPath.forEach { filter ->
                            Text(
                                text = "• ${filter.displayInfo}",
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Filtering State: ${filteringState.name}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Filtered Results",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp, 8.dp)
            )
        }

        // Filtered Videos
        item {
            Text(
                text = "Videos (${filteredData!!.videos.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
            )
        }

        items(safeFilteredData.videos) { video ->
            VideoCard(video = video)
        }

        // Filtered Artists
        item {
            Text(
                text = "Artists (${filteredData!!.artists.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
            )
        }

        items(safeFilteredData.artists) { artist ->
            ArtistCard(artist = artist)
        }

        // Filtered Instruments
        item {
            Text(
                text = "Instruments (${filteredData!!.instruments.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
            )
        }

        items(safeFilteredData.instruments) { instrument ->
            InstrumentCard(instrument = instrument)
        }

        // Filtered Types
        item {
            Text(
                text = "Types (${filteredData!!.types.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
            )
        }

        items(safeFilteredData.types) { type ->
            TypeCard(type = type)
        }

        // Filtered Durations
        item {
            Text(
                text = "Durations (${filteredData!!.durations.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
            )
        }

        items(safeFilteredData.durations) { duration ->
            DurationCard(duration = duration)
        }
    }
}

@Composable
fun ArtistsList(artists: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist>) {
    if (artists.isEmpty()) {
        Text(
            text = "No artists in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(artists) { artist ->
            ArtistCard(artist = artist)
        }
    }
}

@Composable
fun InstrumentsList(instruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument>) {
    if (instruments.isEmpty()) {
        Text(
            text = "No instruments in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(instruments) { instrument ->
            InstrumentCard(instrument = instrument)
        }
    }
}

@Composable
fun QuotesList(quotes: List<com.example.jazzlibraryktroomjpcompose.domain.models.Quote>) {
    if (quotes.isEmpty()) {
        Text(
            text = "No quotes in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(quotes) { quote ->
            QuoteCard(quote = quote)
        }
    }
}

@Composable
fun TypesList(types: List<com.example.jazzlibraryktroomjpcompose.domain.models.Type>) {
    if (types.isEmpty()) {
        Text(
            text = "No types in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(types) { type ->
            TypeCard(type = type)
        }
    }
}

@Composable
fun DurationsList(durations: List<com.example.jazzlibraryktroomjpcompose.domain.models.Duration>) {
    if (durations.isEmpty()) {
        Text(
            text = "No durations in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(durations) { duration ->
            DurationCard(duration = duration)
        }
    }
}

@Composable
fun VideosList(videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video>) {
    if (videos.isEmpty()) {
        Text(
            text = "No videos in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(videos) { video ->
            VideoCard(video = video)
        }
    }
}

@Composable
fun VideoArtistsList(videoArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist>) {
    if (videoArtists.isEmpty()) {
        Text(
            text = "No video-artist associations in database",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn {
        items(videoArtists) { videoArtist ->
            VideoArtistCard(videoArtist = videoArtist)
        }
    }
}

// Individual Card Composables (keep as before)
@Composable
fun ArtistCard(artist: com.example.jazzlibraryktroomjpcompose.domain.models.Artist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${artist.name} ${artist.surname}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${artist.id}, Instrument ID: ${artist.instrumentId}, Rank: ${artist.rank ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InstrumentCard(instrument: com.example.jazzlibraryktroomjpcompose.domain.models.Instrument) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = instrument.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${instrument.id}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun QuoteCard(quote: com.example.jazzlibraryktroomjpcompose.domain.models.Quote) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "ID: ${quote.id}, Artist ID: ${quote.artistId}, Video ID: ${quote.videoId}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TypeCard(type: com.example.jazzlibraryktroomjpcompose.domain.models.Type) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = type.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${type.id}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DurationCard(duration: com.example.jazzlibraryktroomjpcompose.domain.models.Duration) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = duration.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${duration.id}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            duration.description?.let { description ->
                if (description.isNotEmpty()) {
                    Text(
                        text = "Description: $description",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCard(video: com.example.jazzlibraryktroomjpcompose.domain.models.Video) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = video.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "ID: ${video.id}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Duration: ${video.duration}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Type ID: ${video.typeId}, Duration ID: ${video.durationId}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Path: ${video.path}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Location: ${video.locationId}, Availability: ${video.availability}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun VideoArtistCard(videoArtist: com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Video-Artist Association",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Artist ID: ${videoArtist.artistId}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Video ID: ${videoArtist.videoId}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}