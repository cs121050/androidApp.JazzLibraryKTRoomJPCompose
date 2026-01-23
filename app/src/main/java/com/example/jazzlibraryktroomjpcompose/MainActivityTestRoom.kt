package com.example.jazzlibraryktroomjpcompose

import com.example.jazzlibraryktroomjpcompose.ui.theme.JazzLibraryKTRoomJPComposeTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jazzlibraryktroomjpcompose.ui.DatabaseTestViewModel
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

    var testMessage by remember { mutableStateOf("Click buttons to test database") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Room Database Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Test Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.insertTestData()
                    testMessage = "Test data inserted!"
                }
            ) {
                Text("Insert Test Data")
            }

            Button(
                onClick = {
                    viewModel.clearAllData()
                    testMessage = "All data cleared!"
                }
            ) {
                Text("Clear All Data")
            }

            Button(
                onClick = {
                    viewModel.refreshFromDb()
                    testMessage = "Data refreshed from database!"
                }
            ) {
                Text("Refresh")
            }

            Button(
                onClick = {
                    viewModel.testIndividualOperations()
                    testMessage = "Individual operations tested!"
                }
            ) {
                Text("Test CRUD")
            }
        }

        // Status message
        Text(
            text = testMessage,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tabs for different data types
        val tabCount = 7
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
            }
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

// Individual Card Composables
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