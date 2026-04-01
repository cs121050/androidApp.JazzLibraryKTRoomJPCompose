package com.example.jazzlibraryktroomjpcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    private val database: JazzDatabase,
    private val filterManager: FilterManager,
    private val jazzRepository: JazzRepositoryImpl
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

    // New state for filter history
    private val _currentFilterPath = MutableStateFlow<List<FilterPath>>(emptyList())
    val currentFilterPath: StateFlow<List<FilterPath>> = _currentFilterPath.asStateFlow()

    // Timestamp of the currently active history entry
    private var currentStateTimestamp: Long = 0L

    // Current video ID (to store in history)
    private var currentVideoId: Int? = null

    // Filtered data (from FilterManager)
    private val _filteredData = MutableStateFlow<FilterManager.FilteredData?>(null)
    val filteredData: StateFlow<FilterManager.FilteredData?> = _filteredData

    // Loading states
    private val _loadingState = MutableStateFlow(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _statusMessage = MutableStateFlow("Click buttons to test database")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _filteringState = MutableStateFlow(FilteringState.IDLE)
    val filteringState: StateFlow<FilteringState> = _filteringState

    enum class FilteringState {
        IDLE,
        LOADING_FILTERS,
        APPLYING_FILTERS,
        FILTERS_APPLIED
    }

    enum class LoadingState {
        Idle,
        Loading,
        Success,
        Error
    }

    init {
        refreshFromDb()
        loadFilterPath()
    }

    // ------------------------------------------------------------------------
    // API data loading (unchanged)
    // ------------------------------------------------------------------------

    fun loadDataFromApi() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            _statusMessage.value = "Loading data from API..."

            val result = jazzRepository.loadBootstrapData()

            if (result.isSuccess) {
                _loadingState.value = LoadingState.Success
                _statusMessage.value = "Data loaded successfully from API!"
                _errorMessage.value = null

                refreshFromDb()
                clearAllFilters()
            } else {
                _loadingState.value = LoadingState.Error
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _statusMessage.value = "Failed to load data from API"
                _errorMessage.value = "Error: $errorMsg"
            }
        }
    }

    // ------------------------------------------------------------------------
    // Filter history management (updated for serialized strings)
    // ------------------------------------------------------------------------

    private fun loadFilterPath() {
        viewModelScope.launch {
            _filteringState.value = FilteringState.LOADING_FILTERS

            val latest = database.filterPathDao().getLatestFilterPath()
            if (latest != null) {
                val filters = FilterPathMapper.toDomain(latest)
                val enriched = enrichFilterPathNames(filters)
                _currentFilterPath.value = enriched
                currentStateTimestamp = latest.timestamp
                applyFiltersFromPath(enriched)
            } else {
                _currentFilterPath.value = emptyList()
                currentStateTimestamp = 0L
                applyFiltersFromPath(emptyList())
            }
        }
    }

    private suspend fun enrichFilterPathNames(filters: List<FilterPath>): List<FilterPath> {
        return filters.map { filter ->
            if (filter.entityName.isNotEmpty()) return@map filter
            val name = when (filter.categoryId) {
                FilterPath.CATEGORY_INSTRUMENT -> {
                    database.instrumentDao().getInstrumentById(filter.entityId).firstOrNull()?.name ?: ""
                }
                FilterPath.CATEGORY_ARTIST -> {
                    database.artistDao().getArtistById(filter.entityId).firstOrNull()?.name ?: ""
                }
                FilterPath.CATEGORY_DURATION -> {
                    database.durationDao().getDurationById(filter.entityId).firstOrNull()?.name ?: ""
                }
                FilterPath.CATEGORY_TYPE -> {
                    database.typeDao().getTypeById(filter.entityId).firstOrNull()?.name ?: ""
                }
                else -> ""
            }
            filter.copy(entityName = name)
        }
    }

    fun applyFiltersFromPath(filterPaths: List<FilterPath>) {
        viewModelScope.launch {
            _filteringState.value = FilteringState.APPLYING_FILTERS

            filterManager.getFilteredDataFlow(filterPaths)
                .collect { filteredData ->
                    _filteredData.value = filteredData

                    println("=== Filtered Videos (${filteredData.videos.size}) ===")
                    filteredData.videos.forEach { video ->
                        println("- ${video.name}")
                    }
                    println("=============================")

                    _filteringState.value = FilteringState.FILTERS_APPLIED
                }
        }
    }

    fun handleChipAction(
        categoryId: Int,
        entityId: Int,
        entityName: String,
        isSelected: Boolean
    ) {
        viewModelScope.launch {
            val current = _currentFilterPath.value
            val newFilterPath = if (isSelected) {
                filterManager.handleChipSelection(current, categoryId, entityId, entityName)
            } else {
                filterManager.handleChipDeselection(current, categoryId, entityId)
            }

            if (newFilterPath == current) return@launch

            val serial = FilterPathMapper.serialize(newFilterPath)
            val timestamp = System.currentTimeMillis()
            val newEntry = FilterPathMapper.toEntity(serial, currentVideoId, timestamp)
            database.filterPathDao().insertFilterPath(newEntry)
            database.filterPathDao().deleteAllNewerThan(timestamp)

            _currentFilterPath.value = newFilterPath
            currentStateTimestamp = timestamp
            applyFiltersFromPath(newFilterPath)
        }
    }

    // Save filter path to database (creates a new history entry)
    private suspend fun saveFilterPath(filterPaths: List<FilterPath>) {
        val serial = FilterPathMapper.serialize(filterPaths)
        val timestamp = System.currentTimeMillis()
        val newEntry = FilterPathMapper.toEntity(serial, currentVideoId, timestamp)
        database.filterPathDao().insertFilterPath(newEntry)
        database.filterPathDao().deleteAllNewerThan(timestamp)

        _currentFilterPath.value = filterPaths
        currentStateTimestamp = timestamp
        applyFiltersFromPath(filterPaths)
    }

    // Clear all filters (create a new empty history entry)
    fun clearAllFilters() {
        viewModelScope.launch {
            val serial = ""
            val timestamp = System.currentTimeMillis()
            val newEntry = FilterPathMapper.toEntity(serial, currentVideoId, timestamp)
            database.filterPathDao().insertFilterPath(newEntry)
            database.filterPathDao().deleteAllNewerThan(timestamp)

            _currentFilterPath.value = emptyList()
            currentStateTimestamp = timestamp
            applyFiltersFromPath(emptyList())
            _statusMessage.value = "All filters cleared"
        }
    }

    // Set the current video ID (to store in history)
    fun setCurrentVideoId(videoId: Int?) {
        currentVideoId = videoId
    }

    // ------------------------------------------------------------------------
    // Test data insertion and refresh (unchanged)
    // ------------------------------------------------------------------------

    fun insertTestData() {
        viewModelScope.launch {
            _statusMessage.value = "Inserting test data..."

            // Clear existing data first (in reverse order of dependencies)
            database.quoteDao().deleteAllQuotes()
            database.videoContainsArtistDao().deleteAllVideoContainsArtists()
            database.videoDao().deleteAllVideos()
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.typeDao().deleteAllTypes()
            database.durationDao().deleteAllDurations()
            // Also clear filter history (optional)
            database.filterPathDao().deleteAllFilterPaths()

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
                ArtistRoomEntity(1, "Miles", "Davis", 1, 100, "", "", 1),
                ArtistRoomEntity(2, "John", "Coltrane", 2, 95, "", "", 1),
                ArtistRoomEntity(3, "Bill", "Evans", 3, 90, "", "", 1),
                ArtistRoomEntity(4, "Charlie", "Parker", 2, 98, "", "", 1),
                ArtistRoomEntity(5, "Duke", "Ellington", 3, 92, "", "", 1),
                ArtistRoomEntity(6, "Wes", "Montgomery", 6, 85, "", "", 1),
                ArtistRoomEntity(7, "Charles", "Mingus", 4, 88, "", "", 1),
                ArtistRoomEntity(8, "Art", "Blakey", 5, 86, "", "", 1)
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
                VideoContainsArtistRoomEntity(2, 3),  // Bill Evans in Giant Steps
                VideoContainsArtistRoomEntity(3, 4),  // Charlie Parker interview
                VideoContainsArtistRoomEntity(4, 6),  // Wes Montgomery solo
                VideoContainsArtistRoomEntity(5, 7),  // Mingus in documentary
                VideoContainsArtistRoomEntity(5, 8)   // Blakey in documentary
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

            _statusMessage.value = "Test data inserted successfully!"
            refreshFromDb()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _statusMessage.value = "Clearing all data..."

            database.quoteDao().deleteAllQuotes()
            database.videoContainsArtistDao().deleteAllVideoContainsArtists()
            database.videoDao().deleteAllVideos()
            database.artistDao().deleteAllArtists()
            database.instrumentDao().deleteAllInstruments()
            database.typeDao().deleteAllTypes()
            database.durationDao().deleteAllDurations()
            database.filterPathDao().deleteAllFilterPaths()

            _statusMessage.value = "All data cleared!"
            refreshFromDb()
        }
    }

    fun refreshFromDb() {
        viewModelScope.launch {
            _statusMessage.value = "Refreshing data from database..."

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

            _statusMessage.value = "Data refreshed from database!"
        }
    }

    // ------------------------------------------------------------------------
    // Test methods (updated to work with new filter system)
    // ------------------------------------------------------------------------

    fun testAllFilteringQueries() {
        viewModelScope.launch {
            _statusMessage.value = "Testing all filtering queries..."

            println("=== Testing All Filtering Queries ===")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

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
            _statusMessage.value = "Filtering queries test completed!"
        }
    }

    fun testFilterPathOperations() {
        viewModelScope.launch {
            _statusMessage.value = "Testing filter path operations..."

            println("=== Testing Filter Path Operations ===")

            clearAllFilters()

            // Test 1: Add Instrument filter (creates history entry)
            println("\n1. Adding Instrument filter: Saxophone")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 2,
                entityName = "Saxophone",
                isSelected = true
            )
            delayForTest(300)

            // Test 2: Add Type filter (creates another entry)
            println("2. Adding Type filter: Interview")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_TYPE,
                entityId = 3,
                entityName = "Interview",
                isSelected = true
            )
            delayForTest(300)

            // Test 3: Retrieve all filter path history
            val allHistory = database.filterPathDao().getAllFilterPaths().first()
            println("\n3. All filter path history (${allHistory.size} entries):")
            allHistory.forEach { entry ->
                println("  - ${entry.serialNumber} (timestamp: ${entry.timestamp})")
            }

            // Test 4: Clear all filters
            println("\n4. Clearing all filters")
            clearAllFilters()

            // Test 5: Verify we have a new entry
            val latest = database.filterPathDao().getLatestFilterPath()
            println("\n5. Latest filter path after clear:")
            if (latest != null) {
                println("  - Serial: ${latest.serialNumber}")
            } else {
                println("  - None")
            }

            println("\n=== Filter Path Tests Completed ===")
            _statusMessage.value = "Filter path operations test completed!"
        }
    }

    fun testCompleteFilteringScenario() {
        viewModelScope.launch {
            _statusMessage.value = "Testing complete filtering scenario..."

            println("=== Testing Complete Filtering Scenario ===")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            // Scenario: User selects Saxophone (Instrument) and Interview (Type)
            println("\nScenario: User selects Saxophone (Instrument) and Interview (Type)")

            // Add filters
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 2,
                entityName = "Saxophone",
                isSelected = true
            )
            delayForTest(300)

            handleChipAction(
                categoryId = FilterPath.CATEGORY_TYPE,
                entityId = 3,
                entityName = "Interview",
                isSelected = true
            )
            delayForTest(500)

            // Retrieve current filter path
            println("\nCurrent filter path:")
            _currentFilterPath.value.forEach {
                println("  - ${it.displayInfo}")
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

            // Test removing a filter
            println("\n--- Removing Type filter ---")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_TYPE,
                entityId = 3,
                entityName = "Interview",
                isSelected = false
            )
            delayForTest(500)

            val afterRemoval = _currentFilterPath.value
            println("Filters after removal: ${afterRemoval.size}")

            clearAllFilters()
            println("\n=== Complete Filtering Scenario Test Completed ===")
            _statusMessage.value = "Complete filtering scenario test completed!"
        }
    }

    fun testAllCombinedFilterQueries() {
        viewModelScope.launch {
            _statusMessage.value = "Testing all combined filter queries..."

            println("=== Testing All Combined Filter Queries ===")

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
            _statusMessage.value = "Combined filter queries test completed!"
        }
    }

    fun testAmbiguousColumnFix() {
        viewModelScope.launch {
            _statusMessage.value = "Testing ambiguous column fix..."

            println("=== Testing Ambiguous Column Fix ===")

            try {
                if (_artists.value.isEmpty()) {
                    insertTestData()
                }

                println("\n1. Testing Artists by Instrument (Saxophone):")
                val artists = database.artistDao()
                    .getArtistsByInstrumentWithVideoCount(2).first()
                println("✓ Artists by instrument query works: ${artists.size} results")
                artists.take(3).forEach {
                    println("   - ${it.artist.name} ${it.artist.surname} (${it.videoCount} videos)")
                }

                println("\n2. Testing Types by Instrument (Saxophone):")
                val types = database.typeDao()
                    .getTypesByInstrumentWithVideoCount(2).first()
                println("✓ Types by instrument query works: ${types.size} results")
                types.take(3).forEach {
                    println("   - ${it.type.name} (${it.videoCount} videos)")
                }

                println("\n3. Testing Artists by Type and Duration (Interview + Medium):")
                val artistsByTypeDuration = database.artistDao()
                    .getArtistsByTypeAndDurationWithVideoCount(3, 2).first()
                println("✓ Artists by type and duration query works: ${artistsByTypeDuration.size} results")

                println("\n4. Testing Videos by Instrument and Type (Saxophone + Interview):")
                val videos = database.videoDao()
                    .getVideosByInstrumentAndType(2, 3).first()
                println("✓ Videos by instrument and type query works: ${videos.size} results")
                videos.take(3).forEach {
                    println("   - ${it.name}")
                }

                println("\n=== All ambiguous column tests passed! ===")
                _statusMessage.value = "Ambiguous column fix test completed!"

            } catch (e: Exception) {
                println("✗ Error: ${e.message}")
                e.printStackTrace()
                _statusMessage.value = "Error in ambiguous column test: ${e.message}"
            }
        }
    }

    fun testCompositionClasses() {
        viewModelScope.launch {
            _statusMessage.value = "Testing composition classes..."

            println("=== Testing Composition Classes ===")

            try {
                val artists = database.artistDao()
                    .getArtistsByInstrumentWithVideoCount(2).first()
                println("Artists with video count: ${artists.size}")
                _statusMessage.value = "Composition classes test completed!"
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
                _statusMessage.value = "Error in composition classes test: ${e.message}"
            }
        }
    }

    fun testFilterPathScenarios() {
        viewModelScope.launch {
            _statusMessage.value = "Testing filter path scenarios..."

            println("\n=== Testing Filter Path Scenarios ===\n")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            // Scenario 1: Select an instrument chip
            println("Scenario 1: Selecting Saxophone instrument")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 2,
                entityName = "Saxophone",
                isSelected = true
            )
            delayForTest(500)

            // Scenario 2: Add a type filter
            println("\nScenario 2: Adding Interview type filter")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_TYPE,
                entityId = 3,
                entityName = "Interview",
                isSelected = true
            )
            delayForTest(500)

            // Scenario 3: Select an artist (should auto-select instrument)
            println("\nScenario 3: Selecting John Coltrane artist (should auto-select Saxophone)")
            handleChipAction(
                categoryId = FilterPath.CATEGORY_ARTIST,
                entityId = 2,
                entityName = "John Coltrane",
                isSelected = true
            )
            delayForTest(500)

            println("\nCurrent filter path:")
            _currentFilterPath.value.forEach {
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
            delayForTest(500)

            println("\nFilter path after deselection:")
            _currentFilterPath.value.forEach {
                println("  - ${it.displayInfo}")
            }

            // Scenario 5: Test multiple filters
            println("\nScenario 5: Testing multiple simultaneous filters")

            clearAllFilters()

            val testFilters = listOf(
                FilterPath(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2,
                    entityName = "Saxophone"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 2,
                    entityName = "Studio Recording"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_DURATION,
                    entityId = 2,
                    entityName = "Medium"
                )
            )

            saveFilterPath(testFilters)
            delayForTest(1000)

            val filteredData = _filteredData.value
            println("\nResults with 3 filters (Instrument: Saxophone, Type: Studio Recording, Duration: Medium):")
            println("Videos found: ${filteredData?.videos?.size ?: 0}")
            println("Artists found: ${filteredData?.artists?.size ?: 0}")

            // Scenario 6: Test filter persistence
            println("\nScenario 6: Testing filter persistence")
            val savedFilters = database.filterPathDao().getAllFilterPaths().first()
            savedFilters.forEach {
                println("  - ${it.serialNumber} (${it.timestamp})")
            }

            clearAllFilters()

            println("\n=== All Filter Path Scenarios Tested ===")
            _statusMessage.value = "Filter path scenarios test completed!"
        }
    }

    fun testChipGroupLogic() {
        viewModelScope.launch {
            _statusMessage.value = "Testing chip group logic..."

            println("\n=== Testing Chip Group Logic ===\n")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            // Test 1: Only one chip per category can be selected
            println("Test 1: One chip per category rule")

            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 3,
                entityName = "Piano",
                isSelected = true
            )
            delayForTest(300)

            // Try to select Trumpet - should replace Piano
            handleChipAction(
                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                entityId = 1,
                entityName = "Trumpet",
                isSelected = true
            )
            delayForTest(500)

            println("Instrument filter should show Trumpet, not Piano:")
            _currentFilterPath.value.forEach {
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
            delayForTest(500)

            println("Filter path after selecting Bill Evans:")
            _currentFilterPath.value.forEach {
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
            delayForTest(500)

            println("Filter path after deselecting Piano:")
            if (_currentFilterPath.value.isEmpty()) {
                println("  (empty - Bill Evans should also be removed)")
            } else {
                _currentFilterPath.value.forEach {
                    println("  - ${it.displayInfo}")
                }
            }

            // Test 4: Multiple categories work independently
            println("\nTest 4: Multiple independent categories")

            clearAllFilters()

            val testActions = listOf(
                Triple(FilterPath.CATEGORY_INSTRUMENT, 6, "Guitar"),
                Triple(FilterPath.CATEGORY_TYPE, 4, "Documentary"),
                Triple(FilterPath.CATEGORY_DURATION, 5, "Full Concert")
            )

            testActions.forEach { (categoryId, entityId, entityName) ->
                handleChipAction(categoryId, entityId, entityName, true)
                delayForTest(200)
            }
            delayForTest(1000)

            println("Final filter path with 3 different categories:")
            _currentFilterPath.value.forEach {
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
            _statusMessage.value = "Chip group logic test completed!"
        }
    }

    fun testFilteredDataPopulation() {
        viewModelScope.launch {
            _statusMessage.value = "Testing filtered data population..."

            println("\n=== Testing Filtered Data Population ===\n")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            clearAllFilters()

            val testFilters = listOf(
                FilterPath(
                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                    entityId = 2,
                    entityName = "Saxophone"
                ),
                FilterPath(
                    categoryId = FilterPath.CATEGORY_TYPE,
                    entityId = 1,
                    entityName = "Live Performance"
                )
            )

            saveFilterPath(testFilters)
            delayForTest(1000)

            val data = _filteredData.value
            println("Testing filtered data with Saxophone + Live Performance:\n")

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
            _statusMessage.value = "Filtered data population test completed!"
        }
    }

    fun testAppStartupWithExistingFilters() {
        viewModelScope.launch {
            _statusMessage.value = "Testing app startup with existing filters..."

            println("\n=== Testing App Startup with Existing Filters ===\n")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

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

            saveFilterPath(startupFilters)
            println("Filters saved to database (simulating app close):")
            startupFilters.forEach { println("  - ${it.displayInfo}") }

            // Simulate app restart by clearing memory and reloading
            println("\nSimulating app restart...")
            _currentFilterPath.value = emptyList()
            _filteredData.value = null

            // Load filters from database (what happens on app start)
            loadFilterPath()
            delayForTest(1000)

            println("\nFilters loaded after restart:")
            _currentFilterPath.value.forEach { println("  - ${it.displayInfo}") }

            println("\nFiltered data after restart:")
            val filteredVideos = _filteredData.value?.videos ?: emptyList()
            println("Videos found: ${filteredVideos.size}")
            filteredVideos.forEach { println("  - ${it.name}") }

            clearAllFilters()

            println("\n=== App Startup Filter Test Completed ===")
            _statusMessage.value = "App startup filter test completed!"
        }
    }

    fun runAllFilterTests() {
        viewModelScope.launch {
            _statusMessage.value = "Running complete filter tests..."

            println("╔══════════════════════════════════════════╗")
            println("║     RUNNING COMPLETE FILTER TESTS       ║")
            println("╚══════════════════════════════════════════╝")

            if (_artists.value.isEmpty()) {
                insertTestData()
            }

            testFilterPathScenarios()
            delayForTest(1000)

            testChipGroupLogic()
            delayForTest(1000)

            testFilteredDataPopulation()
            delayForTest(1000)

            testAppStartupWithExistingFilters()

            println("\n" + "═".repeat(50))
            println("ALL FILTER TESTS COMPLETED SUCCESSFULLY!")
            println("═".repeat(50))
            _statusMessage.value = "All filter tests completed successfully!"
        }
    }

    fun testEdgeCases() {
        viewModelScope.launch {
            _statusMessage.value = "Testing edge cases..."

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
            delayForTest(500)

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
                delayForTest(100)
            }
            delayForTest(1000)
            println("Final filter after rapid selections:")
            _currentFilterPath.value.forEach { println("  - ${it.displayInfo}") }

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
            delayForTest(500)
            println("Filter should be: Duration: Medium")

            clearAllFilters()
            println("\n=== Edge Cases Test Completed ===")
            _statusMessage.value = "Edge cases test completed!"
        }
    }

    // Helper to delay in tests
    private suspend fun delayForTest(timeMs: Long) {
        kotlinx.coroutines.delay(timeMs)
    }

    // Helper to get all video-artist associations
    suspend fun getAllVideoArtists(): List<VideoContainsArtistRoomEntity> {
        return database.videoContainsArtistDao().getAllVideoContainsArtists().firstOrNull() ?: emptyList()
    }
}