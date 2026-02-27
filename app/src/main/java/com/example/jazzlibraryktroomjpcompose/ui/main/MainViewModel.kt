// MainViewModel.kt - Updated to only fetch API data when database is empty
package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
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

    init {
        checkAndLoadData()
    }

    // NEW: Check if database has data, load from API only if empty
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
                            _uiState.update { it.copy(availableArtists = artists) }
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
            database.filterPathDao().getAllFilterPaths()
                .map { entities -> entities.map { FilterPathMapper.toDomain(it) } }
                .collect { filterPaths ->
                    _filterState.update { it.copy(currentFilterPath = filterPaths) }
                    println("DEBUG: Loaded ${filterPaths.size} filter paths")

                    if (filterPaths.isNotEmpty()) {
                        applyFiltersFromPath(filterPaths)
                    }
//                    else{
//                        // FIX: If path is empty, explicitly clear the filtered UI state
//                        _uiState.update { it.copy(filteredVideos = it.videos) }
//                        _filterState.update { it.copy(isFiltering = false) }
//                    }
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
                        availableArtists = filteredData.artists,
                        availableInstruments = filteredData.instruments,
                        availableDurations = filteredData.durations,
                        availableTypes = filteredData.types
                    )
                }

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
            val currentFilterPath = _filterState.value.currentFilterPath

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

            saveFilterPath(newFilterPath)

            if (newFilterPath.isNotEmpty()) {
                applyFiltersFromPath(newFilterPath)
            } else {
                clearFilters()
            }
        }
    }

    private suspend fun saveFilterPath(filterPaths: List<FilterPath>) {
        database.filterPathDao().deleteAllFilterPaths()

        if (filterPaths.isNotEmpty()) {
            val entities = filterPaths.map { FilterPathMapper.toEntity(it) }
            database.filterPathDao().insertAllFilterPaths(entities)
        }

        _filterState.update { it.copy(currentFilterPath = filterPaths) }
    }

    private fun clearFilters() {
        viewModelScope.launch {
            // 1. Clear the Database
            database.filterPathDao().deleteAllFilterPaths()
            // 2. Stop any active filtering job immediately
            filterJob?.cancel()
            // 3. Reset the Filter State in memory
            _filterState.update {
                it.copy(
                    currentFilterPath = emptyList(),
                    isFiltering = false
                )
            }
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

    fun shuffleVideoList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshTrigger.value += 1  // this is your existing trigger
            // Simulate a tiny delay to make the spinner visible
            delay(300)
            _isRefreshing.value = false
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
}

// UI State classes (unchanged)
data class MainUiState(
    val videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val filteredVideos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video> = emptyList(),
    val allInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist> = emptyList(),
    val availableInstruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument> = emptyList(),
    val availableDurations: List<com.example.jazzlibraryktroomjpcompose.domain.models.Duration> = emptyList(),
    val availableTypes: List<com.example.jazzlibraryktroomjpcompose.domain.models.Type> = emptyList(),
    val availableVideoContainsArtists: List<com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist> = emptyList(),
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