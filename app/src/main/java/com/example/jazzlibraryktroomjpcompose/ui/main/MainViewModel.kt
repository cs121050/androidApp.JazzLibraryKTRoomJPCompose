// MainViewModel.kt (updated – removed PlayerViewModel injection)

package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
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

    // Filter state (only the filtering flag remains)
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Current filter path (single list of active filters)
    private val _currentFilterPath = MutableStateFlow<List<FilterPath>>(emptyList())
    val currentFilterPath: StateFlow<List<FilterPath>> = _currentFilterPath.asStateFlow()

    // Timestamp of the currently active history entry
    private var currentStateTimestamp: Long = 0L

    // History entries (all rows, no consecutive duplicates)
    private val _historyEntries = MutableStateFlow<List<FilterPathRoomEntity>>(emptyList())
    val historyEntries: StateFlow<List<FilterPathRoomEntity>> = _historyEntries.asStateFlow()

    // Event to restore video from history
    private val _restoreHistoryEvent = MutableSharedFlow<RestoreHistoryEvent>()
    val restoreHistoryEvent: SharedFlow<RestoreHistoryEvent> = _restoreHistoryEvent.asSharedFlow()

    // Bottom sheet state
    private val _bottomSheetState = MutableStateFlow(BottomSheetState.HIDDEN)
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    private val _bottomSheetProgress = MutableStateFlow(0f)

    private val _leftDrawerState = MutableStateFlow(DrawerState.CLOSED)
    val leftDrawerState: StateFlow<DrawerState> = _leftDrawerState.asStateFlow()

    // Loading state
    private val _loadingState = MutableStateFlow(LoadingState.IDLE)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // Error handling
    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Refresh
    private val refreshTrigger = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var filterJob: Job? = null

    // Player visibility and card states
    private val _isPlayerVisible = MutableStateFlow(true)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    private val _cardUiStates = MutableStateFlow<Map<String, CardUiState>>(emptyMap())
    val cardUiStates: StateFlow<Map<String, CardUiState>> = _cardUiStates.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.VIDEOS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Current video ID (to be stored in history entries)
    private var currentVideoId: Int? = null

    // Last stored video ID (to avoid duplicate entries)
    private var lastStoredVideoId: Int? = null

    init {
        checkAndLoadData()
    }

    // Called from UI when the player's video ID changes
    fun onVideoIdChanged(newVideoId: Int?) {
        viewModelScope.launch {
            // Ignore if no video or if it's the same as the last stored one
            if (newVideoId == null || newVideoId == lastStoredVideoId) return@launch

            val currentPath = _currentFilterPath.value
            val serial = FilterPathMapper.serialize(currentPath)
            val newTimestamp = System.currentTimeMillis()

            // Delete all entries newer than the current state timestamp
            database.filterPathDao().deleteAllNewerThan(currentStateTimestamp)

            // Insert new entry with the current filter path and new video ID
            val newEntry = FilterPathMapper.toEntity(serial, newVideoId, newTimestamp)
            database.filterPathDao().insertFilterPath(newEntry)

            // Update state variables
            currentStateTimestamp = newTimestamp
            lastStoredVideoId = newVideoId
        }
    }

    // ------------------------------------------------------------------------
    // Initial data loading (unchanged)
    // ------------------------------------------------------------------------

    private fun checkAndLoadData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            val hasData = checkIfDatabaseHasData()
            if (hasData) {
                println("DEBUG: Database has data, loading from local storage")
                _loadingState.value = LoadingState.SUCCESS
                loadInitialData()
                loadFilterPath()
            } else {
                println("DEBUG: Database is empty, fetching from API")
                loadBootstrapData()
            }
        }
    }

    private suspend fun checkIfDatabaseHasData(): Boolean {
        val instrumentCount = database.instrumentDao().getInstrumentCount()
        println("DEBUG: Database check - Instruments: $instrumentCount")
        return instrumentCount > 0
    }

    private fun loadBootstrapData() {
        viewModelScope.launch {
            val result = jazzRepository.loadBootstrapData()
            if (result.isSuccess) {
                _loadingState.value = LoadingState.SUCCESS
                loadInitialData()
                loadFilterPath()
                showSnackbar("Data loaded successfully!")
            } else {
                _loadingState.value = LoadingState.ERROR
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to load data"
                showSnackbar("$errorMsg. Using local data if available.")
                loadInitialData()
                loadFilterPath()
            }
        }
    }

    fun safeRefreshDataFromAPI() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            try {
                if (!checkApiAvailability()) {
                    showSnackbar("API unavailable. Local data preserved.")
                    _loadingState.value = LoadingState.SUCCESS
                    return@launch
                }
                val result = jazzRepository.loadBootstrapData()
                if (result.isSuccess) {
                    loadInitialData()
                    loadFilterPath()
                    showSnackbar("Data refreshed successfully!")
                    _loadingState.value = LoadingState.SUCCESS
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                    showSnackbar("Refresh failed: $errorMsg. Local data preserved.")
                    _loadingState.value = LoadingState.SUCCESS
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}. Local data preserved.")
                _loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private suspend fun checkApiAvailability(): Boolean = try {
        jazzRepository.checkApiConnectivity()
    } catch (e: Exception) {
        false
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _errorMessage.value = message
            _showError.value = true
            delay(4000)
            _showError.value = false
            _errorMessage.value = null
        }
    }

    fun dismissError() {
        _showError.value = false
        _errorMessage.value = null
    }

    // ------------------------------------------------------------------------
    // Data loading from database (unchanged)
    // ------------------------------------------------------------------------

    private fun loadInitialData() {
        viewModelScope.launch {
            val jobs = listOf(
                launch {
                    combine(
                        database.videoDao().getAllVideos()
                            .map { entities -> entities.map { VideoMapper.toDomain(it) } },
                        settingsRepository.randomiseVideoList,
                        refreshTrigger
                    ) { videos, shouldRandomise, _ ->
                        if (shouldRandomise) videos.shuffled() else videos
                    }.collect { randomisedVideos ->
                        _uiState.update { it.copy(videos = randomisedVideos) }
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
                        }
                },
                launch {
                    database.artistDao().getAllArtistsWithVideoCount()
                        .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }
                        .collect { artists ->
                            _uiState.update {
                                it.copy(availableArtists = artists, availableArtistsDisplay = artists)
                            }
                        }
                },
                launch {
                    database.typeDao().getAllTypesWithCount()
                        .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }
                        .collect { types -> _uiState.update { it.copy(availableTypes = types) } }
                },
                launch {
                    database.durationDao().getAllDurationsWithCount()
                        .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }
                        .collect { durations -> _uiState.update { it.copy(availableDurations = durations) } }
                },
                launch {
                    database.videoContainsArtistDao().getAllVideoContainsArtists()
                        .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }
                        .collect { videoContainsArtists ->
                            _uiState.update { it.copy(availableVideoContainsArtists = videoContainsArtists) }
                        }
                }
            )
            jobs.forEach { it.join() }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ------------------------------------------------------------------------
    // Filter history management
    // ------------------------------------------------------------------------

    private fun loadFilterPath() {
        viewModelScope.launch {
            val latest = database.filterPathDao().getLatestFilterPath()
            if (latest != null) {
                val filters = FilterPathMapper.toDomain(latest)
                val enriched = enrichFilterPathNames(filters)
                _currentFilterPath.value = enriched
                currentStateTimestamp = latest.timestamp
                lastStoredVideoId = latest.videoId      // <-- set last stored video ID
                applyFiltersFromPath(enriched)
            } else {
                // No history – start empty
                _currentFilterPath.value = emptyList()
                currentStateTimestamp = 0L
                lastStoredVideoId = null
                applyFiltersFromPath(emptyList())
            }
            // Collect all history for the History tab (without consecutive duplicates)
            database.filterPathDao().getAllFilterPathsWithoutConsecutiveDuplicates()
                .collect { entries ->
                    _historyEntries.value = entries
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

    private fun applyFiltersFromPath(filterPath: List<FilterPath>) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            _filterState.update { it.copy(isFiltering = true) }
            combine(
                filterManager.getFilteredDataFlow(filterPath),
                settingsRepository.randomiseVideoList,
                refreshTrigger
            ) { filteredData, shouldRandomise, _ ->
                filteredData to shouldRandomise
            }.collect { (filteredData, shouldRandomise) ->
                val finalVideos = if (shouldRandomise) filteredData.videos.shuffled() else filteredData.videos
                _uiState.update { uiState ->
                    uiState.copy(
                        filteredVideos = finalVideos,
                        availableArtistsDisplay = filteredData.artists,
                        availableArtists = filteredData.artists,
                        availableInstruments = filteredData.instruments,
                        availableDurations = filteredData.durations,
                        availableTypes = filteredData.types
                    )
                }
                _filterState.update { it.copy(isFiltering = false) }
            }
        }
    }

    // Handle chip selection (non‑suspend – launches coroutine internally)
    fun handleChipSelection(
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

            // If the filter path didn't change, stop
            if (newFilterPath == current) return@launch

            val serial = FilterPathMapper.serialize(newFilterPath)
            val newTimestamp = System.currentTimeMillis()

            // Delete all entries newer than the current state
            database.filterPathDao().deleteAllNewerThan(currentStateTimestamp)

            // Insert the new entry
            val newEntry = FilterPathMapper.toEntity(serial, currentVideoId, newTimestamp)
            database.filterPathDao().insertFilterPath(newEntry)

            // Update state
            _currentFilterPath.value = newFilterPath
            currentStateTimestamp = newTimestamp
            lastStoredVideoId = currentVideoId  // <-- store the current video ID
            applyFiltersFromPath(newFilterPath)
        }
    }

    private suspend fun clearFilters() {
        val serial = ""
        val newTimestamp = System.currentTimeMillis()

        // 1. Delete all entries newer than the current state
        database.filterPathDao().deleteAllNewerThan(currentStateTimestamp)

        // 2. Insert new empty entry
        val newEntry = FilterPathMapper.toEntity(serial, currentVideoId, newTimestamp)
        database.filterPathDao().insertFilterPath(newEntry)

        // 3. Update state
        _currentFilterPath.value = emptyList()
        currentStateTimestamp = newTimestamp
        lastStoredVideoId = currentVideoId  // <-- store the current video ID
        applyFiltersFromPath(emptyList())
        loadInitialData()
    }

    fun clearAllFilters() {
        viewModelScope.launch { clearFilters() }
    }

    // ------------------------------------------------------------------------
    // History navigation
    // ------------------------------------------------------------------------

    fun restoreHistoryState(entry: FilterPathRoomEntity) {
        viewModelScope.launch {
            val filterList = FilterPathMapper.toDomain(entry)
            val enrichedList = enrichFilterPathNames(filterList)
            _currentFilterPath.value = enrichedList
            currentStateTimestamp = entry.timestamp
            lastStoredVideoId = entry.videoId   // <-- set last stored video ID

            // Emit event to restore the video (if any)
            _restoreHistoryEvent.emit(RestoreHistoryEvent(entry.videoId, enrichedList))

            applyFiltersFromPath(enrichedList)
        }
    }

    suspend fun goBack(): Boolean {
        val prevEntry = database.filterPathDao().getPrevFilterPath(currentStateTimestamp)
        return if (prevEntry != null) {
            restoreHistoryState(prevEntry)
            true
        } else {
            false
        }
    }

    // ------------------------------------------------------------------------
    // Video ID tracking (called from UI when player state changes)
    // ------------------------------------------------------------------------

    fun setCurrentVideoId(videoId: Int?) {
        currentVideoId = videoId
    }

    // ------------------------------------------------------------------------
    // UI state helpers (unchanged)
    // ------------------------------------------------------------------------

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

    fun shuffleVideoList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshTrigger.value += 1
            delay(300)
            _isRefreshing.value = false
        }
    }

    fun shuffleArtists() {
        viewModelScope.launch {
            val current = _uiState.value.availableArtistsDisplay
            _uiState.update { it.copy(availableArtistsDisplay = current.shuffled()) }
        }
    }

    fun togglePlayerVisibility() {
        val newValue = !_isPlayerVisible.value
        _isPlayerVisible.value = newValue
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
            isGloballyVisible -> currentState.copy(expanded = !currentState.expanded)
            else -> {
                if (!currentState.showVideo) {
                    CardUiState(showVideo = true, expanded = false)
                } else {
                    currentState.copy(expanded = !currentState.expanded)
                }
            }
        }
        _cardUiStates.update { it + (videoId to newState) }
    }

    fun setCurrentTab(tab: MainTab) {
        _currentTab.value = tab
    }
}

// Event for restoring video from history
data class RestoreHistoryEvent(val videoId: Int?, val filterPath: List<FilterPath>)

// UI state classes (unchanged)
data class MainUiState(
    val videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val filteredVideos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val allInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist> = emptyList(),
    val availableArtistsDisplay: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist> = emptyList(),
    val availableInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableDurations: List<com.example.jazzlibraryktroomjpcompose.domain.models.Duration> = emptyList(),
    val availableTypes: List<com.example.jazzlibraryktroomjpcompose.domain.models.Type> = emptyList(),
    val availableVideoContainsArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class CardUiState(
    val showVideo: Boolean = false,
    val expanded: Boolean = false
)

data class FilterState(
    val isFiltering: Boolean = false
)

enum class DrawerState { OPEN, CLOSED }
enum class LoadingState { IDLE, LOADING, SUCCESS, ERROR }
enum class BottomSheetState { HIDDEN, HALF_EXPANDED, EXPANDED }
enum class MainTab { VIDEOS, ARTISTS, HISTORY }