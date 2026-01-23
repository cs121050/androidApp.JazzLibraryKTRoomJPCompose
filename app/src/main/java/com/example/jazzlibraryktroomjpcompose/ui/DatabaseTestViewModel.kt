package com.example.jazzlibraryktroomjpcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.domain.models.*
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

    private val _types = MutableStateFlow<List<Type>>(emptyList())
    val types: StateFlow<List<Type>> = _types

    private val _durations = MutableStateFlow<List<Duration>>(emptyList())
    val durations: StateFlow<List<Duration>> = _durations

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    private val _videoArtists = MutableStateFlow<List<VideoContainsArtist>>(emptyList())
    val videoArtists: StateFlow<List<VideoContainsArtist>> = _videoArtists

    init {
        refreshFromDb()
    }

    fun insertTestData() {
        viewModelScope.launch {
            // Clear existing data first (in reverse order of dependencies)
            database.quoteDao().deleteAllQuotes()
            database.videoContainsArtistDao().deleteAllVideoContainsArtists()
            database.videoDao().deleteAllVideos()
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.typeDao().deleteAllTypes()
            database.durationDao().deleteAllDurations()

            // Insert test types
            val testTypes = listOf(
                TypeRoomEntity(1, "Live Performance"),
                TypeRoomEntity(2, "Studio Recording"),
                TypeRoomEntity(3, "Interview"),
                TypeRoomEntity(4, "Documentary"),
                TypeRoomEntity(5, "Tutorial")
            )
            database.typeDao().insertAllTypes(testTypes)

            // Insert test durations
            val testDurations = listOf(
                DurationRoomEntity(1, "Short", "Less than 5 minutes"),
                DurationRoomEntity(2, "Medium", "5-15 minutes"),
                DurationRoomEntity(3, "Long", "15-30 minutes"),
                DurationRoomEntity(4, "Extended", "30+ minutes"),
                DurationRoomEntity(5, "Full Concert", "60+ minutes")
            )
            database.durationDao().insertAllDurations(testDurations)

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

            // Insert test videos
            val testVideos = listOf(
                VideoRoomEntity(
                    1, "So What - Live", "9:15", "/videos/so_what.mp4",
                    "NYC_1960", "Available", 3, 1
                ),
                VideoRoomEntity(
                    2, "Giant Steps Studio", "4:45", "/videos/giant_steps.mp4",
                    "LA_1959", "Available", 2, 2
                ),
                VideoRoomEntity(
                    3, "Parker Interview", "12:30", "/videos/parker_interview.mp4",
                    "Chicago_1953", "Available", 2, 3
                ),
                VideoRoomEntity(
                    4, "Wes Montgomery Solo", "7:22", "/videos/wes_solo.mp4",
                    "SF_1965", "Available", 3, 1
                ),
                VideoRoomEntity(
                    5, "Take Five Documentary", "45:00", "/videos/take_five_doc.mp4",
                    "Boston_1961", "Available", 5, 4
                )
            )
            database.videoDao().insertAllVideos(testVideos)

            // Insert test video-artist associations
            val testVideoArtists = listOf(
                VideoContainsArtistRoomEntity(1, 1),  // Miles Davis in So What
                VideoContainsArtistRoomEntity(2, 2),  // Coltrane in Giant Steps
                VideoContainsArtistRoomEntity(3, 2),  // Bill Evans in Giant Steps
                VideoContainsArtistRoomEntity(4, 3),  // Charlie Parker interview
                VideoContainsArtistRoomEntity(6, 4),  // Wes Montgomery solo
                VideoContainsArtistRoomEntity(7, 5),  // Mingus in documentary
                VideoContainsArtistRoomEntity(8, 5)   // Blakey in documentary
            )
            database.videoContainsArtistDao().insertAllVideoContainsArtists(testVideoArtists)

            // Insert test quotes
            val testQuotes = listOf(
                QuoteRoomEntity(id = 1, text = "I'll play it first and tell you what it is later.", videoId = 1, artistId = 1),
                QuoteRoomEntity(id = 2, text = "You can play a shoestring if you're sincere.", videoId = 1, artistId = 1),
                QuoteRoomEntity(id = 3, text = "My music is the spiritual expression of what I am.", videoId = 2, artistId = 2),
                QuoteRoomEntity(id = 4, text = "I know that there are bad times, but that's okay.", videoId = 2, artistId = 2),
                QuoteRoomEntity(id = 5, text = "Jazz is not a what, it is a how.", videoId = 3, artistId = 3),
                QuoteRoomEntity(id = 6, text = "Master your instrument, master the music, and then forget all that bullshit and just play.", videoId = 4, artistId = 4),
                QuoteRoomEntity(id = 7, text = "The piano ain't got no wrong notes.", videoId = 5, artistId = 5)
            )
            database.quoteDao().insertAllQuotes(testQuotes)

            refreshFromDb()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.quoteDao().deleteAllQuotes()
            database.videoContainsArtistDao().deleteAllVideoContainsArtists()
            database.videoDao().deleteAllVideos()
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.typeDao().deleteAllTypes()
            database.durationDao().deleteAllDurations()

            refreshFromDb()
        }
    }

    fun refreshFromDb() {
        viewModelScope.launch {
            // Collect all data in parallel
            val jobs = listOf(
                launch {
                    database.artistDao().getAllArtists()
                        .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
                        .collect { _artists.value = it }
                },
                launch {
                    database.instrumentDao().getAllInstruments()
                        .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }
                        .collect { _instruments.value = it }
                },
                launch {
                    database.quoteDao().getAllQuotes()
                        .map { entities -> entities.map { QuoteMapper.toDomain(it) } }
                        .collect { _quotes.value = it }
                },
                launch {
                    database.typeDao().getAllTypes()
                        .map { entities -> entities.map { TypeMapper.toDomain(it) } }
                        .collect { _types.value = it }
                },
                launch {
                    database.durationDao().getAllDurations()
                        .map { entities -> entities.map { DurationMapper.toDomain(it) } }
                        .collect { _durations.value = it }
                },
                launch {
                    database.videoDao().getAllVideos()
                        .map { entities -> entities.map { VideoMapper.toDomain(it) } }
                        .collect { _videos.value = it }
                },
                launch {
                    database.videoContainsArtistDao().getAllVideoContainsArtists()
                        .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }
                        .collect { _videoArtists.value = it }
                }
            )
            jobs.forEach { it.join() }
        }
    }

    // Test individual CRUD operations for all entities
    fun testIndividualOperations() {
        viewModelScope.launch {
            println("=== Testing Individual CRUD Operations ===")

            // Test Type operations
            println("\n1. Testing Type CRUD:")
            val testType = TypeRoomEntity(100, "Test Type")
            database.typeDao().insertType(testType)

            // Use first() instead of collect() to get a single emission
            val insertedType = database.typeDao().getTypeById(100).first()
            println("Inserted Type: $insertedType")

            // Test Duration operations
            println("\n2. Testing Duration CRUD:")
            val testDuration = DurationRoomEntity(100, "Test Duration", "Test Description")
            database.durationDao().insertDuration(testDuration)

            val insertedDuration = database.durationDao().getDurationById(100).first()
            println("Inserted Duration: $insertedDuration")

            // Test Video operations
            println("\n3. Testing Video CRUD:")
            val testVideo = VideoRoomEntity(
                100, "Test Video", "5:00", "/test/video.mp4",
                "TEST_001", "Available", 1, 1
            )
            database.videoDao().insertVideo(testVideo)

            val insertedVideo = database.videoDao().getVideoById(100).first()
            println("Inserted Video: $insertedVideo")

            // Test Video-Artist association
            println("\n4. Testing Video-Artist Association:")
            val testVideoArtist = VideoContainsArtistRoomEntity(1, 100)
            database.videoContainsArtistDao().insertVideoContainsArtist(testVideoArtist)

            val artistsForVideo = database.videoContainsArtistDao().getArtistsByVideo(100).first()
            println("Video 100 has ${artistsForVideo.size} artists")

            // Test Artist operations
            println("\n5. Testing Artist CRUD:")
            val testArtist = ArtistRoomEntity(100, "Test", "Artist", 1, 50)
            database.artistDao().insertArtist(testArtist)

            val insertedArtist = database.artistDao().getArtistById(100).first()
            println("Inserted Artist: ${insertedArtist.name} ${insertedArtist.surname}")

            // Test update operation
            val updatedArtist = insertedArtist.copy(rank = 75)
            database.artistDao().updateArtist(updatedArtist)
            val afterUpdate = database.artistDao().getArtistById(100).first()
            println("Updated Artist rank: ${afterUpdate.rank}")

            // Test Instrument operations
            println("\n6. Testing Instrument CRUD:")
            val testInstrument = InstrumentRoomEntity(100, "Test Instrument")
            database.instrumentDao().insertInstrument(testInstrument)

            val insertedInstrument = database.instrumentDao().getInstrumentById(100).first()
            println("Inserted Instrument: ${insertedInstrument.name}")

            // Test Quote operations
            println("\n7. Testing Quote CRUD:")
            val testQuote = QuoteRoomEntity(id = 100,
                text = "This is a test quote for CRUD testing.",
                videoId = 100,
                artistId = 100
            )
            database.quoteDao().insertQuote(testQuote)

            // Get the last inserted quote ID (auto-generated)
            val allQuotes = database.quoteDao().getAllQuotes().first()
            val insertedQuote = allQuotes.lastOrNull()
            println("Inserted Quote: ${insertedQuote?.text}")

            // Test search operations
            println("\n8. Testing Search Operations:")
            val searchArtists = database.artistDao().getArtistByName("Test").first()
            println("Found ${searchArtists.size} artists with 'Test' in name")

            val searchVideos = database.videoDao().searchVideosByName("Test").first()
            println("Found ${searchVideos.size} videos with 'Test' in name")

            // Clean up test data
            println("\n9. Cleaning up test data...")
            if (insertedQuote != null) {
                database.quoteDao().deleteQuote(insertedQuote.id)
                println("Deleted test quote")
            }

            database.videoContainsArtistDao().deleteAllArtistsForVideo(100)
            println("Deleted video-artist associations")

            database.videoDao().deleteVideo(insertedVideo)
            println("Deleted test video")

            database.artistDao().deleteArtist(afterUpdate)
            println("Deleted test artist")

            database.instrumentDao().deleteInstrument(insertedInstrument)
            println("Deleted test instrument")

            database.durationDao().deleteDuration(insertedDuration)
            println("Deleted test duration")

            database.typeDao().deleteType(insertedType)
            println("Deleted test type")

            println("\n=== All CRUD tests completed successfully! ===")
            refreshFromDb()
        }
    }

    // Helper function to get all video-artist associations
    suspend fun getAllVideoArtists(): List<VideoContainsArtistRoomEntity> {
        return database.videoContainsArtistDao().getAllVideoContainsArtists().firstOrNull() ?: emptyList()
    }
}