// MainViewModel.kt - Updated to only fetch API data when database is empty
package com.example.jazzlibraryktroomjpcompose.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.ui.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: JazzDatabase,
    private val filterManager: FilterManager,
    private val jazzRepository: JazzRepositoryImpl,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Filter state
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    /// Bottom Sheet State Management
    private val _bottomSheetState = MutableStateFlow(BottomSheetState.HIDDEN)
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    private val _bottomSheetProgress = MutableStateFlow(0f) // 0f = hidden, 0.5f = half, 1f = expanded

    private val _bottomSheetScrollState = MutableStateFlow(0f)

    private val _leftDrawerState = MutableStateFlow(DrawerState.CLOSED)
    val leftDrawerState: StateFlow<DrawerState> = _leftDrawerState.asStateFlow()

    // Loading state
    private val _loadingState = MutableStateFlow(LoadingState.IDLE)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // NEW: Show error as a snackbar/toast, not blocking screen
    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var filterJob: Job? = null

    private val _isPlayerVisible = MutableStateFlow(true)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    private val _cardUiStates = MutableStateFlow<Map<String, CardUiState>>(emptyMap())
    val cardUiStates: StateFlow<Map<String, CardUiState>> = _cardUiStates.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.VIDEOS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Add these variables in MainViewModel
    private val _historyEntries = MutableStateFlow<List<FilterPathRoomEntity>>(emptyList())
    val historyEntries: StateFlow<List<FilterPathRoomEntity>> = _historyEntries.asStateFlow()

    // Keep the current filter path as list for easy use
    private val _currentFilterPath = MutableStateFlow<List<FilterPath>>(emptyList())
    val currentFilterPath: StateFlow<List<FilterPath>> = _currentFilterPath.asStateFlow()

    // Also keep the timestamp of the current state to allow back navigation
    private var currentStateTimestamp: Long = 0L

    private val _currentFilterPathId = MutableStateFlow<Int?>(null)
    val currentFilterPathId: StateFlow<Int?> = _currentFilterPathId.asStateFlow()

    data class HistoryGroupItem(
        val filterPathId: Int,
        val timestamp: Long,
        val filterPaths: List<FilterPath>,      // already enriched with names
        val videos: List<Video>                 // domain Video objects
    )

    private val _enrichedHistory = MutableStateFlow<List<HistoryGroupItem>>(emptyList())
    val enrichedHistory: StateFlow<List<HistoryGroupItem>> = _enrichedHistory.asStateFlow()

    init {
        checkAndLoadData()
    }

    fun hasPreviousHistory(): Boolean {
        val entries = _historyEntries.value
        val currentIndex = entries.indexOfFirst { it.timestamp == currentStateTimestamp }
        return currentIndex != -1 && currentIndex < entries.size - 1
    }

    fun goBack() {
        viewModelScope.launch {
            val entries = _historyEntries.value
            val currentIndex = entries.indexOfFirst { it.timestamp == currentStateTimestamp }
            if (currentIndex != -1 && currentIndex < entries.size - 1) {
                val previous = entries[currentIndex + 1] // list is descending
                restoreHistoryState(previous)
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
                    val artist = database.artistDao().getArtistById(filter.entityId).firstOrNull()
                    if (artist != null) "${artist.name} ${artist.surname}" else ""
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

    // NEW: Check if database has data, load from API only if empty
    private fun checkAndLoadData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING

            val emptyId = ensureInitialFilterPath()
            if (emptyId != null) {
                _currentFilterPathId.value = emptyId
            }

            val hasData = checkIfDatabaseHasData()

            if (hasData) {
                println("DEBUG: Database has data, loading from local storage")
                _loadingState.value = LoadingState.SUCCESS

                ensureInitialFilterPath()

                loadInitialData()
                loadFilterPath()
            } else {
                println("DEBUG: Database is empty, fetching from API")
                loadBootstrapData()
            }
        }
    }

    // NEW: Check if any of the main tables has data
    private suspend fun checkIfDatabaseHasData(): Boolean {
        // Check a representative table (videos or instruments)
        val instrumentCount = database.instrumentDao().getInstrumentCount()

        println("DEBUG: Database check - Instruments: $instrumentCount")

        // Return true if we have at least some data in either table
        return instrumentCount > 0
    }

    // You'll need to add these DAO methods if they don't exist:
    // In VideoDao: @Query("SELECT COUNT(*) FROM video") suspend fun getVideoCount(): Int
    // In InstrumentDao: @Query("SELECT COUNT(*) FROM instrument") suspend fun getInstrumentCount(): Int

    private fun loadBootstrapData() {
        viewModelScope.launch {
            val result = jazzRepository.loadBootstrapData()

            if (result.isSuccess) {
                _loadingState.value = LoadingState.SUCCESS
                // Now load data from database (which now has API data)
                loadInitialData()
                loadFilterPath()
                showSnackbar("Data loaded successfully!")
            } else {
                _loadingState.value = LoadingState.ERROR
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to load data"
                showSnackbar("$errorMsg. Using local data if available.")

                // Even if API fails, try to load any existing data
                loadInitialData()
                loadFilterPath()
            }
        }
    }

    fun safeRefreshDataFromAPI() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING

            try {
                // Check API first
                val apiAvailable = checkApiAvailability()

                if (!apiAvailable) {
                    showSnackbar("API unavailable. Local data preserved.")
                    _loadingState.value = LoadingState.SUCCESS
                    return@launch
                }

                // Fetch fresh data
                val result = jazzRepository.loadBootstrapData()

                if (result.isSuccess) {
                    // Success - update UI
                    loadInitialData()
                    loadFilterPath()
                    showSnackbar("Data refreshed successfully!")
                    _loadingState.value = LoadingState.SUCCESS
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                    showSnackbar("Refresh failed: $errorMsg. Local data preserved.")
                    _loadingState.value = LoadingState.SUCCESS // Still success since we have local data
                }

            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}. Local data preserved.")
                _loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private suspend fun checkApiAvailability(): Boolean {
        return try {
            jazzRepository.checkApiConnectivity()
        } catch (e: Exception) {
            false
        }
    }

    // NEW: Helper to show snackbar messages
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _errorMessage.value = message
            _showError.value = true

            // Auto-hide after 4 seconds
            launch {
                kotlinx.coroutines.delay(4000)
                _showError.value = false
                _errorMessage.value = null
            }
        }
    }

    // NEW: Manual dismiss error
    fun dismissError() {
        _showError.value = false
        _errorMessage.value = null
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Launch separate coroutines for each data type to collect concurrently
            val jobs = listOf(
                launch {
                    combine(
                        database.videoDao().getAllVideos()
                            .map { entities -> entities.map { VideoMapper.toDomain(it) } },
                        settingsRepository.randomiseVideoList,
                        refreshTrigger   // <-- new
                    ) { videos, shouldRandomise, _ ->
                        // The third parameter is the trigger – we ignore its value,
                        // but its emission causes the lambda to run again.
                        if (shouldRandomise) videos.shuffled() else videos
                    }.collect { randomisedVideos ->
                        _uiState.update { it.copy(videos = randomisedVideos) }
                    }
                },
                launch {
                    database.albumDao().getAllAlbums()
                        .map { entities -> entities.map { AlbumMapper.toDomain(it) } }
                        .collect { albums ->
                            _uiState.update { it.copy(albums = albums) }
                            Log.d("AlbumDebug", "Initial load: albums = ${albums.size}")
                        }
                },
                launch {
                    database.instrumentDao().getAllInstrumentsWithArtistCount()
                        .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }
                        .collect { instruments ->
                            _uiState.update { it.copy(
                                allInstruments = instruments,
                                availableInstruments = instruments
                            ) }
                            println("DEBUG: Loaded ${instruments.size} instruments")
                        }
                },
                launch {
                    database.artistDao().getAllArtistsWithVideoCount()
                        .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }
                        .collect { artists ->
                            _uiState.update {
                                it.copy(availableArtists = artists,
                                availableArtistsDisplay = artists)
                            }
                            println("DEBUG: Loaded ${artists.size} artists")
                        }
                },
                launch {
                    database.typeDao().getAllTypesWithCount()
                        .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }
                        .collect { types ->
                            _uiState.update { it.copy(availableTypes = types) }
                            println("DEBUG: Loaded ${types.size} types")
                        }
                },
                launch {
                    database.durationDao().getAllDurationsWithCount()
                        .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }
                        .collect { durations ->
                            _uiState.update { it.copy(availableDurations = durations) }
                            println("DEBUG: Loaded ${durations.size} durations")
                        }
                },
                launch {
                    database.videoContainsArtistDao().getAllVideoContainsArtists()
                        .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }
                        .collect { videoContainsArtists ->
                            _uiState.update { it.copy(availableVideoContainsArtists = videoContainsArtists) }
                            println("DEBUG: Loaded ${videoContainsArtists.size} video-artist associations")
                        }
                },
                launch {
                    database.albumContainsArtistDao().getAllAlbumContainsArtists()
                        .map { entities -> entities.map { AlbumContainsArtistMapper.toDomain(it) } }
                        .collect { albumContainsArtists ->
                            _uiState.update { it.copy(availableAlbumContainsArtists = albumContainsArtists) }
                            println("DEBUG: Loaded ${albumContainsArtists.size} album-artist associations")
                        }
                }
            )

            // Wait for all coroutines to complete their initial collection
            jobs.forEach { it.join() }

            // Update loading state
            _uiState.update { it.copy(isLoading = false) }
            println("DEBUG: Finished loading all data")
        }
    }

    private fun loadFilterPath() {
        viewModelScope.launch {
            val latest = database.filterPathDao().getLatestFilterPath()
            if (latest != null) {
                val filters = FilterPathMapper.toDomain(latest)
                val enriched = enrichFilterPathNames(filters)
                _currentFilterPath.value = enriched
                currentStateTimestamp = latest.timestamp
                applyFiltersFromPath(enriched)

                _currentFilterPathId.value = latest.id
            } else {
                // No history, start with empty filters
                _currentFilterPath.value = emptyList()
                currentStateTimestamp = 0L
                applyFiltersFromPath(emptyList())

                _currentFilterPathId.value = null
            }
            // Load all history for the History tab
            database.filterPathDao().getAllFilterPaths().collect { entries ->
                _historyEntries.value = entries
            }
        }
    }

    private fun applyFiltersFromPath(filterPaths: List<FilterPath>) {
        // Cancel previous job to avoid multiple collectors
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            _filterState.update { it.copy(isFiltering = true) }

            combine(
                filterManager.getFilteredDataFlow(filterPaths),
                settingsRepository.randomiseVideoList,
                refreshTrigger
            ) { filteredData, shouldRandomise, _ ->
                filteredData to shouldRandomise
            }.collect { (filteredData, shouldRandomise) ->
                val finalVideos = if (shouldRandomise) filteredData.videos.shuffled() else filteredData.videos

                _uiState.update { uiState ->
                    uiState.copy(
                        filteredVideos = finalVideos,
                        filteredAlbums = filteredData.albums,
                        availableArtistsDisplay = filteredData.artists,
                        availableArtists = filteredData.artists,
                        availableInstruments = filteredData.instruments,
                        availableDurations = filteredData.durations,
                        availableTypes = filteredData.types
                    )

                }
                Log.d("AlbumDebug", "Filters applied: filterPath = $filterPaths, filteredAlbums size = ${filteredData.albums.size}")


                _filterState.update { filterState ->
                    filterState.copy(
                        currentFilterPath = filteredData.filterPath,
                        isFiltering = false
                    )
                }

                // Optional logging
            }
        }
    }

    fun handleChipSelection(
        categoryId: Int,
        entityId: Int,
        entityName: String,
        isSelected: Boolean
    ) {
        viewModelScope.launch {
            // Use the current filter path from the state we are on
            val currentFilterPath = _currentFilterPath.value

            val newFilterPath = if (isSelected) {
                filterManager.handleChipSelection(
                    currentFilterPath,
                    categoryId,
                    entityId,
                    entityName
                )
            } else {
                filterManager.handleChipDeselection(
                    currentFilterPath,
                    categoryId,
                    entityId
                )
            }

            // No change? Exit.
            if (newFilterPath == currentFilterPath) return@launch

            val serial = FilterPathMapper.serialize(newFilterPath)
            val timestamp = System.currentTimeMillis()

            // Delete any forward history (entries newer than the current state)
            if (currentStateTimestamp > 0L) {
                database.filterPathDao().deleteAllNewerThan(currentStateTimestamp)
            }

            // Insert the new entry
            val newEntry = FilterPathMapper.toEntity(serial, timestamp)
            val insertedId = database.filterPathDao().insertFilterPathAndGetId(newEntry) // returns Long
            _currentFilterPathId.value = insertedId.toInt()

            //make sure the history tab list reload real tiome if it is open
            loadEnrichedHistory()

            // Update local state
            _currentFilterPath.value = newFilterPath
            currentStateTimestamp = timestamp
            applyFiltersFromPath(newFilterPath)
            _filterState.update { it.copy(currentFilterPath = newFilterPath) }
        }
    }


    private fun clearFilters() {
        viewModelScope.launch {
            // Delete any forward history (entries newer than the current state)
            if (currentStateTimestamp > 0L) {
                database.filterPathDao().deleteAllNewerThan(currentStateTimestamp)
            }

            val serial = ""
            val timestamp = System.currentTimeMillis()
            val newEntry = FilterPathMapper.toEntity(serial, timestamp)
            val insertedId = database.filterPathDao().insertFilterPathAndGetId(newEntry)
            _currentFilterPathId.value = insertedId.toInt()

            _currentFilterPath.value = emptyList()
            currentStateTimestamp = timestamp
            applyFiltersFromPath(emptyList())
            _filterState.update { it.copy(currentFilterPath = emptyList()) }
            loadInitialData()
        }
    }

    // Toggle sheet with proper state transitions
    fun toggleBottomSheet() {
        val current = _bottomSheetState.value
        _bottomSheetState.value = when (current) {
            BottomSheetState.HIDDEN -> BottomSheetState.HALF_EXPANDED
            BottomSheetState.HALF_EXPANDED -> BottomSheetState.HIDDEN
            BottomSheetState.EXPANDED -> BottomSheetState.HALF_EXPANDED
        }
        _bottomSheetProgress.value = when (_bottomSheetState.value) {
            BottomSheetState.HIDDEN -> 0f
            BottomSheetState.HALF_EXPANDED -> 0.5f
            BottomSheetState.EXPANDED -> 1f
        }
    }

    fun setBottomSheetState(state: BottomSheetState) {
        _bottomSheetState.value = state
        _bottomSheetProgress.value = when (state) {
            BottomSheetState.HIDDEN -> 0f
            BottomSheetState.HALF_EXPANDED -> 0.5f
            BottomSheetState.EXPANDED -> 1f
        }
    }

    fun updateBottomSheetProgress(progress: Float) {
        _bottomSheetProgress.value = progress.coerceIn(0f, 1f)
    }

    fun toggleLeftDrawer() {
        _leftDrawerState.value = when (_leftDrawerState.value) {
            DrawerState.OPEN -> DrawerState.CLOSED
            DrawerState.CLOSED -> DrawerState.OPEN
        }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilters()
        }
    }

    fun togglePlayerVisibility() {
        val newValue = !_isPlayerVisible.value
        _isPlayerVisible.value = newValue
        // When hiding players globally, reset all per‑card showVideo flags
        if (!newValue) {
            _cardUiStates.update { map ->
                map.mapValues { it.value.copy(showVideo = false) }
            }
        }
    }

    fun onCardTitleClick(videoId: String) {
        val currentMap = _cardUiStates.value
        val currentState = currentMap[videoId] ?: CardUiState()
        val isGloballyVisible = _isPlayerVisible.value

        val newState = when {
            // Global toggle ON → clicking toggles only expanded state
            isGloballyVisible -> currentState.copy(expanded = !currentState.expanded)
            // Global toggle OFF → first click shows video, second click toggles expanded
            else -> {
                if (!currentState.showVideo) {
                    // Video hidden → show video (and keep expanded false)
                    CardUiState(showVideo = true, expanded = false)
                } else {
                    // Video visible → toggle expanded
                    currentState.copy(expanded = !currentState.expanded)
                }
            }
        }

        _cardUiStates.update { it + (videoId to newState) }
    }

    fun setCurrentTab(tab: MainTab) {
        _currentTab.value = tab
    } //(Later you can trigger data loading for the selected tab here)



    private fun restoreHistoryState(entry: FilterPathRoomEntity) {
        viewModelScope.launch {
            val filterList = FilterPathMapper.toDomain(entry)
            val enrichedList = enrichFilterPathNames(filterList)
            _currentFilterPath.value = enrichedList
            _currentFilterPathId.value = entry.id
            currentStateTimestamp = entry.timestamp

            applyFiltersFromPath(enrichedList)
            _filterState.update { it.copy(currentFilterPath = enrichedList) }
        }
    }

    fun loadEnrichedHistory() {
        viewModelScope.launch {
            val rawEntries = database.filterPathDao().getAllHistoryEntries()
            // Group by filterPathId
            val grouped = rawEntries.groupBy { it.filterPathId }
            val result = mutableListOf<HistoryGroupItem>()
            for ((filterPathId, entries) in grouped) {
                val first = entries.first()
                // Deserialize filter paths and enrich names
                val filterPaths = FilterPathMapper.deserialize(first.serialNumber)
                val enrichedPaths = enrichFilterPathNames(filterPaths)
                // Collect videos (distinct by videoId)
                val videos = entries.mapNotNull { entry ->
                    entry.videoId?.let { videoId ->
                        // Fetch full Video domain object from database
                        val videoEntity = database.videoDao().getVideoById(videoId).firstOrNull()
                        videoEntity?.let { VideoMapper.toDomain(it) }
                    }
                }.distinctBy { it.id }
                result.add(HistoryGroupItem(
                    filterPathId = filterPathId,
                    timestamp = first.timestamp,
                    filterPaths = enrichedPaths,
                    videos = videos
                ))
            }
            _enrichedHistory.value = result.sortedByDescending { it.timestamp }
        }
    }

    // MainViewModel.kt
    fun restoreFilterPathFromGroupItem(item: HistoryGroupItem) {
        viewModelScope.launch {
            // Create a temporary FilterPathRoomEntity to reuse restore logic
            val entity = FilterPathRoomEntity(
                id = item.filterPathId,
                serialNumber = FilterPathMapper.serialize(item.filterPaths),
                timestamp = item.timestamp
            )
            restoreHistoryState(entity)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            // 1. Delete all rows from filter_path table
            database.filterPathDao().deleteAll()

            // 2. Insert a new empty filter path (serialNumber = "") as the current state
            val emptyEntry = FilterPathRoomEntity(
                serialNumber = "",
                timestamp = System.currentTimeMillis()
            )
            val newId = database.filterPathDao().insertFilterPathAndGetId(emptyEntry)
            _currentFilterPathId.value = newId.toInt()

            // 3. Reset the UI state to empty filters
            _currentFilterPath.value = emptyList()
            currentStateTimestamp = emptyEntry.timestamp
            applyFiltersFromPath(emptyList())

            // 4. Refresh the history UI (enriched list)
            loadEnrichedHistory()

            // 5. Also refresh the raw history entries flow (if used elsewhere)
            database.filterPathDao().getAllFilterPaths().collect { entries ->
                _historyEntries.value = entries
            }
        }
    }

    fun shuffleVideoList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshTrigger.value += 1  // this is your existing trigger
            // Simulate a tiny delay to make the spinner visible
            // i dont need sufling, it just fetches new presufled set from db, each set is suffled by default
            delay(300)
            _isRefreshing.value = false
        }
    }

    fun shuffleArtists() {
        viewModelScope.launch {
            // Shuffle the current display list
            val current = _uiState.value.availableArtistsDisplay
            val shuffled = current.shuffled()
            _uiState.update { it.copy(availableArtistsDisplay = shuffled) }
        }
    }

    fun shuffleAlbums() {
        viewModelScope.launch {
            val current = _uiState.value.albums
            val shuffled = current.shuffled()
            _uiState.update { it.copy(albums = shuffled) }
            Log.d("AlbumDebug", "Shuffled albums")
        }
    }

    // MainViewModel.kt
    fun refreshHistory() {
        viewModelScope.launch {
            loadEnrichedHistory()   // reload from database
            // Small delay to show the spinner (optional)

        }
    }

    private suspend fun ensureInitialFilterPath(): Int? {
        val count = database.filterPathDao().getCount()
        return if (count == 0) {
            val emptyEntry = FilterPathRoomEntity(
                serialNumber = "",
                timestamp = System.currentTimeMillis()
            )
            //Inserted empty filter path with id
            val id = database.filterPathDao().insertFilterPathAndGetId(emptyEntry)
            id.toInt()
        } else {
            // Get the latest entry's ID
            database.filterPathDao().getLatestFilterPath()?.id
        }
    }

}

// UI State classes (unchanged)
data class MainUiState(
    val videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val filteredVideos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val albums: List<Album> = emptyList(),        // always the full set (shuffled if needed)
    val filteredAlbums: List<Album> = emptyList(),
    val allInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist> = emptyList(), // natural order, used for chips
    val availableArtistsDisplay: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist> = emptyList(),   // display order (shuffled or base)
    val availableInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableDurations: List<com.example.jazzlibraryktroomjpcompose.domain.models.Duration> = emptyList(),
    val availableTypes: List<com.example.jazzlibraryktroomjpcompose.domain.models.Type> = emptyList(),
    val availableVideoContainsArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist> = emptyList(),
    val availableAlbumContainsArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist> = emptyList(),
    val isLoading: Boolean = false, // General UI loading (any operation) (USER POINT OF VIEW LOADING)
    val errorMessage: String? = null
)

data class CardUiState(
    val showVideo: Boolean = false,   // whether video section is visible when global toggle is off
    val expanded: Boolean = false     // whether extra details are shown
)

data class FilterState(
    val currentFilterPath: List<FilterPath> = emptyList(),
    val isFiltering: Boolean = false  // Filter-specific loading (local operation)
)

enum class DrawerState {
    OPEN, CLOSED
}

enum class LoadingState {
    IDLE,           // No API operation in progress
    LOADING,        // API data is being fetched
    SUCCESS,        // API data fetched successfully
    ERROR           // API data fetch failed
}

// Add this enum near DrawerState
enum class BottomSheetState {
    HIDDEN,
    HALF_EXPANDED,
    EXPANDED
}

enum class AlbumSortType {
    RELEASE_DATE_ASC,
    RELEASE_DATE_DESC,
    RATING_ASC,
    RATING_DESC
}

enum class TypeOfMedia {
    EDUCATIONAL,
    ALBUM
}


enum class MainTab { VIDEOS, ARTISTS, HISTORY }