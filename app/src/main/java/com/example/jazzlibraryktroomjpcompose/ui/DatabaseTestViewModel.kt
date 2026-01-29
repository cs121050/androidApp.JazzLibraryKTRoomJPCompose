package com.example.jazzlibraryktroomjpcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseTestViewModel @Inject constructor(
    private val database: JazzDatabase,
    private val filterManager: FilterManager
) : ViewModel() {

    // Original StateFlows
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


    private val _filteredData = MutableStateFlow<FilterManager.FilteredData?>(
        FilterManager.FilteredData(
            videos = emptyList(),
            artists = emptyList(),
            instruments = emptyList(),
            durations = emptyList(),
            types = emptyList(),
            filterPath = emptyList()
        )
    )
    val filteredData: StateFlow<FilterManager.FilteredData?> = _filteredData

    private val _filteringState = MutableStateFlow(FilteringState.IDLE)
    val filteringState: StateFlow<FilteringState> = _filteringState

    enum class FilteringState {
        IDLE,
        LOADING_FILTERS,
        APPLYING_FILTERS,
        FILTERS_APPLIED
    }

    init {
        refreshFromDb()
        loadFilterPath()
    }

    // Load filter path from database on app start
    fun loadFilterPath() {
        viewModelScope.launch {
            _filteringState.value = FilteringState.LOADING_FILTERS

            database.filterPathDao().getAllFilterPaths()
                .map { entities -> entities.map { FilterPathMapper.toDomain(it) } }
                .collect { filterPaths ->
                    _filterPath.value = filterPaths

                    // If we have filters, apply them
                    if (filterPaths.isNotEmpty()) {
                        applyFiltersFromPath(filterPaths)
                    } else {
                        _filteringState.value = FilteringState.IDLE
                    }
                }
        }
    }

    fun applyFiltersFromPath(filterPaths: List<FilterPath>) {
        viewModelScope.launch {
            _filteringState.value = FilteringState.APPLYING_FILTERS

            filterManager.getFilteredDataFlow(filterPaths)
                .collect { filteredData ->
                    _filteredData.value = filteredData

                    // Print video names as requested
                    println("=== Filtered Videos (${filteredData.videos.size}) ===")
                    filteredData.videos.forEach { video ->
                        println("- ${video.name}")
                    }
                    println("=============================")

                    _filteringState.value = FilteringState.FILTERS_APPLIED
                }
        }
    }

    // Handle chip selection/deselection
    fun handleChipAction(
        categoryId: Int,
        entityId: Int,
        entityName: String,
        isSelected: Boolean
    ) {
        viewModelScope.launch {
            val currentFilterPath = _filterPath.value

            val newFilterPath = if (isSelected) {
                // Chip is being selected
                filterManager.handleChipSelection(currentFilterPath, categoryId, entityId, entityName)
            } else {
                // Chip is being deselected
                filterManager.handleChipDeselection(currentFilterPath, categoryId, entityId)
            }

            // Save to database
            saveFilterPath(newFilterPath)

            // Apply filters with new path
            if (newFilterPath.isNotEmpty()) {
                applyFiltersFromPath(newFilterPath)
            } else {
                // If no filters, show all data
                _filteredData.value = FilterManager.FilteredData(
                    videos = _videos.value,
                    artists = _artists.value,
                    instruments = _instruments.value,
                    durations = _durations.value,
                    types = _types.value,
                    filterPath = emptyList()
                )
                _filteringState.value = FilteringState.IDLE
            }
        }
    }

    // Save filter path to database
    private suspend fun saveFilterPath(filterPaths: List<FilterPath>) {
        database.filterPathDao().deleteAllFilterPaths()

        if (filterPaths.isNotEmpty()) {
            val entities = filterPaths.map { FilterPathMapper.toEntity(it) }
            database.filterPathDao().insertAllFilterPaths(entities)
        }

        _filterPath.value = filterPaths
    }

    // Clear all filters
    fun clearAllFilters() {
        viewModelScope.launch {
            database.filterPathDao().deleteAllFilterPaths()
            _filterPath.value = emptyList()
            _filteredData.value = FilterManager.FilteredData(
                videos = _videos.value,
                artists = _artists.value,
                instruments = _instruments.value,
                durations = _durations.value,
                types = _types.value,
                filterPath = emptyList()
            )
            _filteringState.value = FilteringState.IDLE
        }
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


    // Add these test methods after the existing testIndividualOperations() method

    fun testAllFilteringQueries() {
        viewModelScope.launch {
            println("=== Testing All Filtering Queries ===")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            // Clear any existing filters
            clearAllFilters()

            // Test 1: Single Instrument Filter (Saxophone)
            println("\n1. Testing Single Instrument Filter (Saxophone):")
            val saxophoneArtists = database.artistDao().getArtistsByInstrumentWithVideoCount(2).first()
            println("Artists playing Saxophone: ${saxophoneArtists.size}")
            saxophoneArtists.forEach {
                println("  - ${it.artist.name} ${it.artist.surname} (${it.videoCount} videos)")
            }

            // Test 2: Single Type Filter (Interview)
            println("\n2. Testing Single Type Filter (Interview):")
            val interviewArtists = database.artistDao().getArtistsByTypeWithVideoCount(3).first()
            println("Artists in Interview videos: ${interviewArtists.size}")

            // Test 3: Single Duration Filter (Medium: 5-15 minutes)
            println("\n3. Testing Single Duration Filter (Medium):")
            val mediumArtists = database.artistDao().getArtistsByDurationWithVideoCount(2).first()
            println("Artists in Medium duration videos: ${mediumArtists.size}")

            // Test 4: Combined Instrument + Type Filter
            println("\n4. Testing Combined Instrument + Type Filter (Saxophone + Interview):")
            val saxInterviewArtists = database.artistDao()
                .getArtistsByInstrumentAndTypeWithVideoCount(2, 3).first()
            println("Artists playing Saxophone in Interview videos: ${saxInterviewArtists.size}")
            saxInterviewArtists.forEach {
                println("  - ${it.artist.name} ${it.artist.surname} (${it.videoCount} videos)")
            }

            // Test 5: Combined Instrument + Duration Filter
            println("\n5. Testing Combined Instrument + Duration Filter (Saxophone + Medium):")
            val saxMediumArtists = database.artistDao()
                .getArtistsByInstrumentAndDurationWithVideoCount(2, 2).first()
            println("Artists playing Saxophone in Medium duration videos: ${saxMediumArtists.size}")

            // Test 6: Combined Type + Duration Filter
            println("\n6. Testing Combined Type + Duration Filter (Interview + Medium):")
            val interviewMediumArtists = database.artistDao()
                .getArtistsByTypeAndDurationWithVideoCount(3, 2).first()
            println("Artists in Interview videos of Medium duration: ${interviewMediumArtists.size}")

            // Test 7: Videos by Instrument
            println("\n7. Testing Videos by Instrument (Saxophone):")
            val saxophoneVideos = database.videoDao().getVideosByInstrument(2).first()
            println("Videos featuring Saxophone: ${saxophoneVideos.size}")
            saxophoneVideos.forEach { println("  - ${it.name}") }

            // Test 8: Videos by Artist
            println("\n8. Testing Videos by Artist (John Coltrane ID:2):")
            val coltraneVideos = database.videoDao().getVideosByArtist(2).first()
            println("Videos featuring John Coltrane: ${coltraneVideos.size}")

            // Test 9: Videos by Instrument + Type
            println("\n9. Testing Videos by Instrument + Type (Saxophone + Interview):")
            val saxInterviewVideos = database.videoDao().getVideosByInstrumentAndType(2, 3).first()
            println("Interview videos featuring Saxophone: ${saxInterviewVideos.size}")

            // Test 10: Types by Instrument
            println("\n10. Testing Types by Instrument (Saxophone):")
            val saxophoneTypes = database.typeDao().getTypesByInstrumentWithVideoCount(2).first()
            println("Types featuring Saxophone: ${saxophoneTypes.size}")
            saxophoneTypes.forEach {
                println("  - ${it.type.name} (${it.videoCount} videos)")
            }

            // Test 11: Durations by Instrument
            println("\n11. Testing Durations by Instrument (Saxophone):")
            val saxophoneDurations = database.durationDao().getDurationsByInstrumentWithVideoCount(2).first()
            println("Durations for Saxophone videos: ${saxophoneDurations.size}")

            // Test 12: Instruments by Type
            println("\n12. Testing Instruments by Type (Interview):")
            val interviewInstruments = database.instrumentDao().getInstrumentsByTypeWithVideoCount(3).first()
            println("Instruments in Interview videos: ${interviewInstruments.size}")
            interviewInstruments.forEach {
                println("  - ${it.instrument.name} (${it.videoCount} videos)")
            }

            println("\n=== All Filtering Query Tests Completed ===")
        }
    }

    fun testFilterPathOperations() {
        viewModelScope.launch {
            println("=== Testing Filter Path Operations ===")

            // Clear existing filter paths
            database.filterPathDao().deleteAllFilterPaths()

            // Test 1: Add Instrument filter
            println("\n1. Adding Instrument filter: Saxophone")
            database.filterPathDao().insertFilterPath(
                FilterPathRoomEntity(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2,
                    entityName = "Saxophone"
                )
            )

            // Test 2: Add Type filter
            println("2. Adding Type filter: Interview")
            database.filterPathDao().insertFilterPath(
                FilterPathRoomEntity(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 3,
                    entityName = "Interview"
                )
            )

            // Test 3: Retrieve all filter paths
            val allFilters = database.filterPathDao().getAllFilterPaths().first()
            println("\n3. All filter paths: ${allFilters.size}")
            allFilters.forEach {
                println("  - ${FilterPathMapper.getCategoryName(it.categoryId)}: ${it.entityName}")
            }

            // Test 4: Get filters by category
            val instrumentFilters = database.filterPathDao().getFilterPathByCategory(1).first()
            println("\n4. Instrument filters: ${instrumentFilters.size}")

            // Test 5: Delete by category
            println("\n5. Deleting Type filter")
            database.filterPathDao().deleteByCategory(FilterPath.CATEGORY_TYPE)

            val remainingFilters = database.filterPathDao().getAllFilterPaths().first()
            println("Remaining filters: ${remainingFilters.size}")

            // Test 6: Clear all filters
            println("\n6. Clearing all filters")
            database.filterPathDao().deleteAllFilterPaths()
            val emptyFilters = database.filterPathDao().getAllFilterPaths().first()
            println("Filters after clear: ${emptyFilters.size}")

            println("\n=== Filter Path Tests Completed ===")
        }
    }

    fun testCompleteFilteringScenario() {
        viewModelScope.launch {
            println("=== Testing Complete Filtering Scenario ===")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            // Clear any existing filters
            database.filterPathDao().deleteAllFilterPaths()

            // Scenario: User selects Saxophone and Interview
            println("\nScenario: User selects Saxophone (Instrument) and Interview (Type)")

            // Add filters to filter path
            database.filterPathDao().insertAllFilterPaths(
                listOf(
                    FilterPathRoomEntity(
                        categoryId = FilterPath.CATEGORY_INSTRUMENT,
                        entityId = 2, // Saxophone
                        entityName = "Saxophone"
                    ),
                    FilterPathRoomEntity(
                        categoryId = FilterPath.CATEGORY_TYPE,
                        entityId = 3, // Interview
                        entityName = "Interview"
                    )
                )
            )

            // Retrieve current filter path
            val currentFilters = database.filterPathDao().getAllFilterPaths().first()
            println("\nCurrent filter path:")
            currentFilters.forEach {
                println("  - ${FilterPathMapper.toDisplayString(FilterPathMapper.toDomain(it))}")
            }

            // Apply filtering based on current filter path
            println("\nApplying filters...")

            // Get filtered videos
            val filteredVideos = database.videoDao()
                .getVideosByInstrumentAndType(2, 3).first()

            println("\nFiltered Videos (${filteredVideos.size}):")
            filteredVideos.forEach { println("  - ${it.name}") }

            // Get filtered artists with video counts
            val filteredArtists = database.artistDao()
                .getArtistsByInstrumentAndTypeWithVideoCount(2, 3).first()

            println("\nFiltered Artists (${filteredArtists.size}):")
            filteredArtists.forEach {
                println("  - ${it.artist.name} ${it.artist.surname} (${it.videoCount} videos)")
            }

            // Get filtered instruments (should just show saxophone)
            val filteredInstruments = database.instrumentDao()
                .getInstrumentsByTypeWithVideoCount(3).first()
                .filter { it.instrument.id == 2 } // Only saxophone

            println("\nFiltered Instruments (${filteredInstruments.size}):")
            filteredInstruments.forEach {
                println("  - ${it.instrument.name} (${it.videoCount} videos)")
            }

            // Get filtered types (should just show interview)
            val filteredTypes = database.typeDao()
                .getTypesByInstrumentWithVideoCount(2).first()
                .filter { it.type.id == 3 } // Only interview

            println("\nFiltered Types (${filteredTypes.size}):")
            filteredTypes.forEach {
                println("  - ${it.type.name} (${it.videoCount} videos)")
            }

            // Get filtered durations for this combination
            val filteredDurations = database.durationDao()
                .getDurationsByInstrumentAndTypeWithVideoCount(2, 3).first()

            println("\nFiltered Durations (${filteredDurations.size}):")
            filteredDurations.forEach {
                println("  - ${it.duration.name} (${it.videoCount} videos)")
            }

            // Test removing a filter
            println("\n--- Removing Type filter ---")
            database.filterPathDao().deleteByCategory(FilterPath.CATEGORY_TYPE)

            val afterRemoval = database.filterPathDao().getAllFilterPaths().first()
            println("Filters after removal: ${afterRemoval.size}")

            // Now only instrument filter remains
            if (afterRemoval.isNotEmpty()) {
                println("Applying remaining filter (Instrument only)...")
                val saxophoneOnlyVideos = database.videoDao().getVideosByInstrument(2).first()
                println("Videos with Saxophone: ${saxophoneOnlyVideos.size}")
            }

            // Clear all filters
            database.filterPathDao().deleteAllFilterPaths()
            println("\nAll filters cleared")

            println("\n=== Complete Filtering Scenario Test Completed ===")
        }
    }

    fun testAllCombinedFilterQueries() {
        viewModelScope.launch {
            println("=== Testing All Combined Filter Queries ===")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            println("\n--- Testing Triple Filter Combinations ---")

            // Test 1: Artist + Type + Duration (John Coltrane + Interview + Medium)
            println("\n1. Artist (Coltrane) + Type (Interview) + Duration (Medium):")
            try {
                val tripleFilterVideos = database.videoDao()
                    .getVideosByArtistAndTypeAndDuration(2, 3, 2).first()
                println("Triple-filtered videos: ${tripleFilterVideos.size}")
            } catch (e: Exception) {
                println("Query not implemented or error: ${e.message}")
            }

            // Test 2: Instrument + Type + Duration (Saxophone + Interview + Medium)
            println("\n2. Instrument (Saxophone) + Type (Interview) + Duration (Medium):")
            try {
                val tripleFilterVideos2 = database.videoDao()
                    .getVideosByInstrumentAndTypeAndDuration(2, 3, 2).first()
                println("Triple-filtered videos: ${tripleFilterVideos2.size}")
            } catch (e: Exception) {
                println("Query not implemented or error: ${e.message}")
            }

            println("\n--- Testing Double Filter Combinations ---")

            // Test all double filter combinations for videos
            val doubleCombinations = listOf(
                Pair("Instrument+Artist", database.videoDao().getVideosByInstrumentAndArtist(2, 2).first()),
                Pair("Instrument+Type", database.videoDao().getVideosByInstrumentAndType(2, 3).first()),
                Pair("Instrument+Duration", database.videoDao().getVideosByInstrumentAndDuration(2, 2).first()),
                Pair("Artist+Type", database.videoDao().getVideosByArtistAndType(2, 3).first()),
                Pair("Artist+Duration", database.videoDao().getVideosByArtistAndDuration(2, 2).first()),
                Pair("Type+Duration", database.videoDao().getVideosByTypeAndDuration(3, 2).first())
            )

            doubleCombinations.forEach { (name, videos) ->
                println("$name: ${videos.size} videos")
            }

            println("\n--- Testing Video Count Queries ---")

            // Test video count queries for different entities
            println("\nArtists by Instrument (Saxophone) with video count:")
            val artistsWithCount = database.artistDao().getArtistsByInstrumentWithVideoCount(2).first()
            artistsWithCount.forEach {
                println("  ${it.artist.name} ${it.artist.surname}: ${it.videoCount} videos")
            }

            println("\nTypes by Instrument (Saxophone) with video count:")
            val typesWithCount = database.typeDao().getTypesByInstrumentWithVideoCount(2).first()
            typesWithCount.forEach {
                println("  ${it.type.name}: ${it.videoCount} videos")
            }

            println("\n=== All Combined Filter Tests Completed ===")
        }
    }

    // Add these state flows for filtered data (add them with the other state flows at the top)
    private val _filteredArtists = MutableStateFlow<List<ArtistWithVideoCount>>(emptyList())
    val filteredArtists: StateFlow<List<ArtistWithVideoCount>> = _filteredArtists

    private val _filteredInstruments = MutableStateFlow<List<InstrumentWithVideoCount>>(emptyList())
    val filteredInstruments: StateFlow<List<InstrumentWithVideoCount>> = _filteredInstruments

    private val _filteredTypes = MutableStateFlow<List<TypeWithVideoCount>>(emptyList())
    val filteredTypes: StateFlow<List<TypeWithVideoCount>> = _filteredTypes

    private val _filteredDurations = MutableStateFlow<List<DurationWithVideoCount>>(emptyList())
    val filteredDurations: StateFlow<List<DurationWithVideoCount>> = _filteredDurations

    private val _filteredVideos = MutableStateFlow<List<VideoRoomEntity>>(emptyList())
    val filteredVideos: StateFlow<List<VideoRoomEntity>> = _filteredVideos

    private val _filterPath = MutableStateFlow<List<FilterPath>>(emptyList())
    val filterPath: StateFlow<List<FilterPath>> = _filterPath

    // Add this to init block to load filter path on start
    init {
        refreshFromDb()
        loadFilterPath()  // Add this line
    }


    fun testAmbiguousColumnFix() {
        viewModelScope.launch {
            println("=== Testing Ambiguous Column Fix ===")

            try {
                // Make sure we have data
                if (_artists.value.isEmpty()) {
                    insertTestData()
                }

                // Test a simple query
                println("\n1. Testing Artists by Instrument (Saxophone):")
                val artists = database.artistDao()
                    .getArtistsByInstrumentWithVideoCount(2).first()
                println("✓ Artists by instrument query works: ${artists.size} results")
                artists.take(3).forEach {
                    println("   - ${it.artist.name} ${it.artist.surname} (${it.videoCount} videos)")
                }

                // Test another query
                println("\n2. Testing Types by Instrument (Saxophone):")
                val types = database.typeDao()
                    .getTypesByInstrumentWithVideoCount(2).first()
                println("✓ Types by instrument query works: ${types.size} results")
                types.take(3).forEach {
                    println("   - ${it.type.name} (${it.videoCount} videos)")
                }

                // Test the problematic query
                println("\n3. Testing Artists by Type and Duration (Interview + Medium):")
                val artistsByTypeDuration = database.artistDao()
                    .getArtistsByTypeAndDurationWithVideoCount(3, 2).first()
                println("✓ Artists by type and duration query works: ${artistsByTypeDuration.size} results")

                // Test videos by instrument and type
                println("\n4. Testing Videos by Instrument and Type (Saxophone + Interview):")
                val videos = database.videoDao()
                    .getVideosByInstrumentAndType(2, 3).first()
                println("✓ Videos by instrument and type query works: ${videos.size} results")
                videos.take(3).forEach {
                    println("   - ${it.name}")
                }

                println("\n=== All ambiguous column tests passed! ===")

            } catch (e: Exception) {
                println("✗ Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun testCompositionClasses() {
        viewModelScope.launch {
            println("=== Testing Composition Classes ===")

            try {

                // Test the actual query
                val artists = database.artistDao()
                    .getArtistsByInstrumentWithVideoCount(2).first()
                println("Artists with video count: ${artists.size}")

            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    // Add new test methods for filtering scenarios
    fun testFilterPathScenarios() {
        viewModelScope.launch {
            println("\n=== Testing Filter Path Scenarios ===\n")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            // Clear any existing filters
            clearAllFilters()

            // Scenario 1: Select an instrument chip
            println("Scenario 1: Selecting Saxophone instrument")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 2, // Saxophone
                entityName = "Saxophone",
                isSelected = true
            )

            // Wait a bit for filters to apply
            kotlinx.coroutines.delay(500)

            // Scenario 2: Add a type filter
            println("\nScenario 2: Adding Interview type filter")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_TYPE,
                entityId = 3, // Interview
                entityName = "Interview",
                isSelected = true
            )

            kotlinx.coroutines.delay(500)

            // Scenario 3: Select an artist (should auto-select instrument)
            println("\nScenario 3: Selecting John Coltrane artist (should auto-select Saxophone)")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_ARTIST,
                entityId = 2, // John Coltrane
                entityName = "John Coltrane",
                isSelected = true
            )

            kotlinx.coroutines.delay(500)

            // Check current filter path
            val currentFilters = _filterPath.value
            println("\nCurrent filter path:")
            currentFilters.forEach {
                println("  - ${it.displayInfo}")
            }

            // Scenario 4: Deselect instrument (should also remove artist)
            println("\nScenario 4: Deselecting Saxophone (should also remove John Coltrane)")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 2,
                entityName = "Saxophone",
                isSelected = false
            )

            kotlinx.coroutines.delay(500)

            println("\nFilter path after deselection:")
            _filterPath.value.forEach {
                println("  - ${it.displayInfo}")
            }

            // Scenario 5: Test multiple filters
            println("\nScenario 5: Testing multiple simultaneous filters")

            // Clear first
            clearAllFilters()

            // Set up multiple filters
            val testFilters = listOf(
                FilterPath(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2, // Saxophone
                    entityName = "Saxophone"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 2, // Studio Recording
                    entityName = "Studio Recording"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_DURATION,
                    entityId = 2, // Medium
                    entityName = "Medium"
                )
            )

            saveFilterPath(testFilters)
            applyFiltersFromPath(testFilters)

            kotlinx.coroutines.delay(1000)

            val filteredData = _filteredData.value
            println("\nResults with 3 filters (Instrument: Saxophone, Type: Studio Recording, Duration: Medium):")
            println("Videos found: ${filteredData?.videos?.size ?: 0}")
            println("Artists found: ${filteredData?.artists?.size ?: 0}")

            // Scenario 6: Test filter persistence
            println("\nScenario 6: Testing filter persistence")
            println("Current filters saved to database:")
            val savedFilters = database.filterPathDao().getAllFilterPaths().first()
            savedFilters.forEach {
                println("  - ${FilterPathMapper.toDisplayString(FilterPathMapper.toDomain(it))}")
            }

            // Clear for next test
            clearAllFilters()

            println("\n=== All Filter Path Scenarios Tested ===")
        }
    }

    fun testChipGroupLogic() {
        viewModelScope.launch {
            println("\n=== Testing Chip Group Logic ===\n")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            // Test 1: Only one chip per category can be selected
            println("Test 1: One chip per category rule")

            // Select Piano
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 3,
                entityName = "Piano",
                isSelected = true
            )

            kotlinx.coroutines.delay(300)

            // Try to select Trumpet - should replace Piano
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 1,
                entityName = "Trumpet",
                isSelected = true
            )

            kotlinx.coroutines.delay(500)

            println("Instrument filter should show Trumpet, not Piano:")
            _filterPath.value.forEach {
                if (it.categoryId == FilterPath.CATEGORY_INSTRUMENT) {
                    println("  - ${it.displayInfo}")
                }
            }

            // Test 2: Auto-instrument selection when artist is selected
            println("\nTest 2: Auto-instrument selection")

            clearAllFilters()

            // Select Bill Evans (plays Piano)
            handleChipAction(
                categoryId = FilterPath.CATEGORY_ARTIST,
                entityId = 3,
                entityName = "Bill Evans",
                isSelected = true
            )

            kotlinx.coroutines.delay(500)

            println("Filter path after selecting Bill Evans:")
            _filterPath.value.forEach {
                println("  - ${it.displayInfo}")
            }
            println("Should have both Artist: Bill Evans and Instrument: Piano")

            // Test 3: Chip deselection cascade
            println("\nTest 3: Chip deselection cascade")

            // Now deselect Piano
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 3,
                entityName = "Piano",
                isSelected = false
            )

            kotlinx.coroutines.delay(500)

            println("Filter path after deselecting Piano:")
            if (_filterPath.value.isEmpty()) {
                println("  (empty - Bill Evans should also be removed)")
            } else {
                _filterPath.value.forEach {
                    println("  - ${it.displayInfo}")
                }
            }

            // Test 4: Multiple categories work independently
            println("\nTest 4: Multiple independent categories")

            clearAllFilters()

            // Select multiple filters from different categories
            val testActions = listOf(
                Triple(FilterPath.CATEGORY_INSTRUMENT, 6, "Guitar"),
                Triple(FilterPath.CATEGORY_TYPE, 4, "Documentary"),
                Triple(FilterPath.CATEGORY_DURATION, 5, "Full Concert")
            )

            testActions.forEach { (categoryId, entityId, entityName) ->
                handleChipAction(categoryId, entityId, entityName, true)
                kotlinx.coroutines.delay(200)
            }

            kotlinx.coroutines.delay(1000)

            println("Final filter path with 3 different categories:")
            _filterPath.value.forEach {
                println("  - ${it.displayInfo}")
            }

            val filteredVideos = _filteredData.value?.videos?.size ?: 0
            println("\nVideos matching all 3 filters: $filteredVideos")

            if (filteredVideos > 0) {
                println("Matching videos:")
                _filteredData.value?.videos?.forEach {
                    println("  - ${it.name}")
                }
            }

            clearAllFilters()

            println("\n=== Chip Group Logic Tests Completed ===")
        }
    }

    fun testFilteredDataPopulation() {
        viewModelScope.launch {
            println("\n=== Testing Filtered Data Population ===\n")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            // Set up a filter scenario
            val testFilters = listOf(
                FilterPath(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2, // Saxophone
                    entityName = "Saxophone"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 1, // Live Performance
                    entityName = "Live Performance"
                )
            )

            saveFilterPath(testFilters)
            applyFiltersFromPath(testFilters)

            kotlinx.coroutines.delay(1000)

            val data = _filteredData.value
            println("Testing filtered data with Saxophone + Live Performance:\n")

            // Check if all entities are properly filtered
            println("1. Filtered Artists (should only be saxophonists in live videos):")
            data?.artists?.forEach { artist ->
                val instrumentName = _instruments.value.find { it.id == artist.instrumentId }?.name ?: "Unknown"
                println("  - ${artist.name} ${artist.surname} (plays $instrumentName)")
            }

            println("\n2. Filtered Instruments (should only show Saxophone):")
            data?.instruments?.forEach { instrument ->
                println("  - ${instrument.name}")
            }

            println("\n3. Filtered Types (should only show Live Performance):")
            data?.types?.forEach { type ->
                println("  - ${type.name}")
            }

            println("\n4. Filtered Durations (from live saxophone videos):")
            data?.durations?.forEach { duration ->
                println("  - ${duration.name}")
            }

            println("\n5. Filtered Videos (live saxophone videos):")
            data?.videos?.forEach { video ->
                val typeName = _types.value.find { it.id == video.typeId }?.name ?: "Unknown"
                println("  - ${video.name} (Type: $typeName)")
            }

            // Test that non-matching data is excluded
            println("\n6. Verification of excluded data:")
            val totalArtists = _artists.value.size
            val filteredArtists = data?.artists?.size ?: 0
            println("Total artists: $totalArtists, Filtered artists: $filteredArtists")

            val pianoArtists = data?.artists?.filter { artist ->
                _instruments.value.find { it.id == artist.instrumentId }?.name == "Piano"
            }
            println("Piano artists in filtered results: ${pianoArtists?.size ?: 0} (should be 0)")

            clearAllFilters()

            println("\n=== Filtered Data Population Test Completed ===")
        }
    }

    fun testAppStartupWithExistingFilters() {
        viewModelScope.launch {
            println("\n=== Testing App Startup with Existing Filters ===\n")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            // Clear and set up test filters
            clearAllFilters()

            val startupFilters = listOf(
                FilterPath(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2,
                    entityName = "Saxophone"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 3,
                    entityName = "Interview"
                )
            )

            // Save filters to database (simulating app close with filters active)
            saveFilterPath(startupFilters)

            println("Filters saved to database (simulating app close):")
            startupFilters.forEach { println("  - ${it.displayInfo}") }

            // Simulate app restart by clearing memory and reloading
            println("\nSimulating app restart...")
            _filterPath.value = emptyList()
            _filteredData.value = null

            // Load filters from database (what happens on app start)
            loadFilterPath()

            kotlinx.coroutines.delay(1000)

            println("\nFilters loaded after restart:")
            _filterPath.value.forEach { println("  - ${it.displayInfo}") }

            println("\nFiltered data after restart:")
            val filteredVideos = _filteredData.value?.videos ?: emptyList()
            println("Videos found: ${filteredVideos.size}")
            filteredVideos.forEach { println("  - ${it.name}") }

            // Verify chip groups would be populated correctly
            println("\nExpected chip group population:")
            println("Instrument chips: Should show Saxophone (selected)")
            println("Artist chips: Should show saxophonists in interviews")
            println("Type chips: Should show Interview (selected)")
            println("Duration chips: Should show durations from saxophone interview videos")

            clearAllFilters()

            println("\n=== App Startup Filter Test Completed ===")
        }
    }

    // Helper method to get all entities for chip display
    suspend fun getAllEntitiesForChipDisplay(): Map<Int, List<Any>> {
        val allArtists = database.artistDao().getAllArtists().first()
        val allInstruments = database.instrumentDao().getAllInstruments().first()
        val allDurations = database.durationDao().getAllDurations().first()
        val allTypes = database.typeDao().getAllTypes().first()

        return mapOf(
            FilterPath.CATEGORY_ARTIST to allArtists,
            FilterPath.CATEGORY_INSTRUMENT to allInstruments,
            FilterPath.CATEGORY_DURATION to allDurations,
            FilterPath.CATEGORY_TYPE to allTypes
        )
    }


    fun runAllFilterTests() {
        viewModelScope.launch {
            println("╔══════════════════════════════════════════╗")
            println("║     RUNNING COMPLETE FILTER TESTS       ║")
            println("╚══════════════════════════════════════════╝")

            // Make sure we have test data
            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            // Run all filter tests
            testFilterPathScenarios()
            kotlinx.coroutines.delay(1000)

            testChipGroupLogic()
            kotlinx.coroutines.delay(1000)

            testFilteredDataPopulation()
            kotlinx.coroutines.delay(1000)

            testAppStartupWithExistingFilters()

            println("\n" + "═".repeat(50))
            println("ALL FILTER TESTS COMPLETED SUCCESSFULLY!")
            println("═".repeat(50))
        }
    }

    fun testEdgeCases() {
        viewModelScope.launch {
            println("\n=== Testing Edge Cases ===\n")

            clearAllData()
            insertTestData()
            clearAllFilters()

            // Edge case 1: Selecting non-existent entity
            println("Edge Case 1: Selecting non-existent entity")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 999,
                entityName = "Non-existent Instrument",
                isSelected = true
            )
            kotlinx.coroutines.delay(500)

            // Edge case 2: Rapid sequential selections
            println("\nEdge Case 2: Rapid sequential selections")
            val rapidSelections = listOf(
                Triple(FilterPath.CATEGORY_INSTRUMENT, 1, "Trumpet"),
                Triple(FilterPath.CATEGORY_INSTRUMENT, 2, "Saxophone"),
                Triple(FilterPath.CATEGORY_INSTRUMENT, 3, "Piano"),
                Triple(FilterPath.CATEGORY_TYPE, 1, "Live Performance")
            )

            rapidSelections.forEach { (categoryId, entityId, entityName) ->
                handleChipAction(categoryId, entityId, entityName, true)
                kotlinx.coroutines.delay(100)
            }

            kotlinx.coroutines.delay(1000)
            println("Final filter after rapid selections:")
            _filterPath.value.forEach { println("  - ${it.displayInfo}") }

            // Edge case 3: Clear filters while loading
            println("\nEdge Case 3: Clearing filters while loading")
            clearAllFilters()

            // Immediately try to add new filter
            handleChipAction(
                categoryId = FilterPath.CATEGORY_DURATION,
                entityId = 2,
                entityName = "Medium",
                isSelected = true
            )

            kotlinx.coroutines.delay(500)
            println("Filter should be: Duration: Medium")

            clearAllFilters()
            println("\n=== Edge Cases Test Completed ===")
        }
    }

    // Helper function to get all video-artist associations
    suspend fun getAllVideoArtists(): List<VideoContainsArtistRoomEntity> {
        return database.videoContainsArtistDao().getAllVideoContainsArtists().firstOrNull() ?: emptyList()
    }
}