// MainViewModel.kt - Updated to only fetch API data when database is empty
package com.example.jazzlibraryktroomjpcompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.FilterManager
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: JazzDatabase,
    private val filterManager: FilterManager,
    private val jazzRepository: JazzRepositoryImpl
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Filter state
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Drawer state
    private val _rightDrawerState = MutableStateFlow(DrawerState.CLOSED)
    val rightDrawerState: StateFlow<DrawerState> = _rightDrawerState.asStateFlow()

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

    fun safeRefreshData() {
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
                    database.videoDao().getAllVideos()
                        .map { entities -> entities.map { VideoMapper.toDomain(it) } }
                        .collect { videos ->
                            _uiState.update { it.copy(videos = videos) }
                            println("DEBUG: Loaded ${videos.size} videos")
                        }
                },
                launch {
                    database.instrumentDao().getAllInstruments()
                        .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }
                        .collect { instruments ->
                            _uiState.update { it.copy(
                                allInstruments = instruments,
                                availableInstruments = instruments
                            ) }
                            println("DEBUG: Loaded ${instruments.size} instruments")
                        }
                },
                launch {
                    database.artistDao().getAllArtists()
                        .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
                        .collect { artists ->
                            _uiState.update { it.copy(availableArtists = artists) }
                            println("DEBUG: Loaded ${artists.size} artists")
                        }
                },
                launch {
                    database.typeDao().getAllTypes()
                        .map { entities -> entities.map { TypeMapper.toDomain(it) } }
                        .collect { types ->
                            _uiState.update { it.copy(availableTypes = types) }
                            println("DEBUG: Loaded ${types.size} types")
                        }
                },
                launch {
                    database.durationDao().getAllDurations()
                        .map { entities -> entities.map { DurationMapper.toDomain(it) } }
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
                }
        }
    }

    private fun applyFiltersFromPath(filterPaths: List<FilterPath>) {
        viewModelScope.launch {
            // Show filtering indicator in UI (not API loading)
            _filterState.update { it.copy(isFiltering = true) }

            filterManager.getFilteredDataFlow(filterPaths)
                .collect { filteredData ->
                    _uiState.update { uiState ->
                        uiState.copy(
                            filteredVideos = filteredData.videos,
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

                    println("=== Filtered Videos (${filteredData.videos.size}) ===")
                    filteredData.videos.forEach { video ->
                        println("- ${video.name}")
                    }
                    println("=============================")
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
            database.filterPathDao().deleteAllFilterPaths()
            _filterState.update {
                it.copy(
                    currentFilterPath = emptyList(),
                    isFiltering = false
                )
            }
            loadInitialData()
        }
    }

    // NEW: Clear all data from database
    private suspend fun clearAllData() {
        database.quoteDao().deleteAllQuotes()
        database.videoContainsArtistDao().deleteAllVideoContainsArtists()
        database.videoDao().deleteAllVideos()
        database.artistDao().deleteAllArtists()
        database.instrumentDao().deleteAllInstruments()
        database.typeDao().deleteAllTypes()
        database.durationDao().deleteAllDurations()
        database.filterPathDao().deleteAllFilterPaths()

        println("DEBUG: Cleared all data from database")
    }

    fun toggleRightDrawer() {
        _rightDrawerState.value = when (_rightDrawerState.value) {
            DrawerState.OPEN -> DrawerState.CLOSED
            DrawerState.CLOSED -> DrawerState.OPEN
        }
    }

    fun toggleLeftDrawer() {
        _leftDrawerState.value = when (_leftDrawerState.value) {
            DrawerState.OPEN -> DrawerState.CLOSED
            DrawerState.CLOSED -> DrawerState.OPEN
        }
    }

    fun closeDrawers() {
        _rightDrawerState.value = DrawerState.CLOSED
        _leftDrawerState.value = DrawerState.CLOSED
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilters()
        }
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