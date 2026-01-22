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
        }

        // Status message
        Text(
            text = testMessage,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tabs for different data types
        var selectedTab by remember { mutableStateOf(0) }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            ) {
                Text("Artists (${artists.size})")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            ) {
                Text("Instruments (${instruments.size})")
            }
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            ) {
                Text("Quotes (${quotes.size})")
            }
        }

        // Display data based on selected tab
        when (selectedTab) {
            0 -> ArtistsList(artists = artists)
            1 -> InstrumentsList(instruments = instruments)
            2 -> QuotesList(quotes = quotes)
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
                        text = "ID: ${artist.id}, Instrument ID: ${artist.instrumentId}, Rank: ${artist.rank}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
                        text = "ID: ${quote.id}, Artist ID: ${quote.artistId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}