package com.example.jazzlibraryktroomjpcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.QuoteMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import com.example.jazzlibraryktroomjpcompose.domain.models.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseTestViewModel @Inject constructor(
    private val database: JazzDatabase
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())
    val instruments: StateFlow<List<Instrument>> = _instruments

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes

    init {
        refreshFromDb()
    }

    fun insertTestData() {
        viewModelScope.launch {
            // Clear existing data first
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.quoteDao().deleteAllQuotes()

            // Insert test instruments
            val testInstruments = listOf(
                InstrumentRoomEntity(1, "Trumpet"),
                InstrumentRoomEntity(2, "Saxophone"),
                InstrumentRoomEntity(3, "Piano"),
                InstrumentRoomEntity(4, "Bass"),
                InstrumentRoomEntity(5, "Drums"),
                InstrumentRoomEntity(6, "Guitar")
            )
            database.instrumentDao().insertAllInstruments(testInstruments)

            // Insert test artists
            val testArtists = listOf(
                ArtistRoomEntity(1, "Miles", "Davis", 1, 100),
                ArtistRoomEntity(2, "John", "Coltrane", 2, 95),
                ArtistRoomEntity(3, "Bill", "Evans", 3, 90),
                ArtistRoomEntity(4, "Charlie", "Parker", 2, 98),
                ArtistRoomEntity(5, "Duke", "Ellington", 3, 92),
                ArtistRoomEntity(6, "Wes", "Montgomery", 6, 85),
                ArtistRoomEntity(7, "Charles", "Mingus", 4, 88),
                ArtistRoomEntity(8, "Art", "Blakey", 5, 86)
            )
            database.artistDao().insertAllArtists(testArtists)

            // Insert test quotes
            val testQuotes = listOf(
                QuoteRoomEntity(1, "I'll play it first and tell you what it is later.", 1),
                QuoteRoomEntity(2, "You can play a shoestring if you're sincere.", 1),
                QuoteRoomEntity(3, "My music is the spiritual expression of what I am.", 2),
                QuoteRoomEntity(4, "I know that there are bad times, but that's okay.", 2),
                QuoteRoomEntity(5, "Jazz is not a what, it is a how.", 3),
                QuoteRoomEntity(6, "Master your instrument, master the music, and then forget all that bullshit and just play.", 4)
            )
            database.quoteDao().insertAllQuotes(testQuotes)

            refreshFromDb()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.quoteDao().deleteAllQuotes()

            refreshFromDb()
        }
    }

    fun refreshFromDb() {
        viewModelScope.launch {
            // Collect artists
            launch {
                database.artistDao().getAllArtists()
                    .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
                    .collect { artistsList ->
                        _artists.value = artistsList
                    }
            }

            // Collect instruments
            launch {
                database.instrumentDao().getAllInstruments()
                    .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }
                    .collect { instrumentsList ->
                        _instruments.value = instrumentsList
                    }
            }

            // Collect quotes
            launch {
                database.quoteDao().getAllQuotes()
                    .map { entities -> entities.map { QuoteMapper.toDomain(it) } }
                    .collect { quotesList ->
                        _quotes.value = quotesList
                    }
            }
        }
    }

    // Test individual CRUD operations
    fun testIndividualOperations() {
        viewModelScope.launch {
            // 1. Test insert single artist
            val newArtist = ArtistRoomEntity(100, "Test", "Musician", 1, 50)
            database.artistDao().insertArtist(newArtist)

            // 2. Test get by ID - need to collect the Flow
            var retrievedArtist: ArtistRoomEntity? = null
            database.artistDao().getArtistById(100).collect { artist ->
                retrievedArtist = artist
                println("Retrieved artist: $artist")
            }

            // 3. Test update
            retrievedArtist?.let {
                val updatedArtist = it.copy(rank = 75)
                database.artistDao().updateArtist(updatedArtist)
            }

            // 4. Test search
            val searchResults = database.artistDao().searchArtists("Miles")
            searchResults.collect { artists ->
                println("Search results for 'Miles': ${artists.size} artists")
            }

            // 5. Test delete
            retrievedArtist?.let {
                // Need to get the updated artist first since we modified it
                var artistToDelete: ArtistRoomEntity? = null
                database.artistDao().getArtistById(100).collect { artist ->
                    artistToDelete = artist
                }
                artistToDelete?.let { artist ->
                    database.artistDao().deleteArtist(artist)
                }
            }

            refreshFromDb()
        }
    }
}