// MainViewModel.kt - Updated to only fetch API data when database is empty
package com.example.jazzlibraryktroomjpcompose.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import com.example.jazzlibraryktroomjpcompose.domain.FilterOrchestrator
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.repository.AlbumRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.AssociationRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.DurationRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterPathRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.InstrumentRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.SearchHistoryRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.SongRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.TypeRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.VideoRepository
import com.example.jazzlibraryktroomjpcompose.ui.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val artistRepository: ArtistRepository,
    private val instrumentRepository: InstrumentRepository,
    private val albumRepository: AlbumRepository,
    private val durationRepository: DurationRepository,
    private val typeRepository: TypeRepository,
    private val songRepository: SongRepository,
    private val associationRepository: AssociationRepository,
    private val filterPathRepository: FilterPathRepository,
    private val jazzRepository: JazzRepositoryImpl,
    private val filterOrchestrator: FilterOrchestrator,
    private val settingsRepository: SettingsRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private var lastBackPressTime = 0L

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


    // Keep the current filter path as list for easy use
    private val _currentFilterPath = MutableStateFlow<List<FilterPath>>(emptyList())
    val currentFilterPath: StateFlow<List<FilterPath>> = _currentFilterPath.asStateFlow()

    // Also keep the timestamp of the current state to allow back navigation
    private var currentStateTimestamp: Long = 0L

    private val _currentFilterPathId = MutableStateFlow<Int?>(null)
    val currentFilterPathId: StateFlow<Int?> = _currentFilterPathId.asStateFlow()

    data class AlbumArtistInfo(
        val artist: Artist,
        val isMain: Boolean
    )

    // Fullscreen state (derived from orientation + player visibility)
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _showBars = MutableStateFlow(false)
    val showBars: StateFlow<Boolean> = _showBars.asStateFlow()

    private val _currentAlbumId = MutableStateFlow<Int?>(null)
    val currentAlbumId: StateFlow<Int?> = _currentAlbumId.asStateFlow()

    private val _albumSongs = MutableStateFlow<List<Song>>(emptyList())
    val albumSongs: StateFlow<List<Song>> = _albumSongs.asStateFlow()

    private val _isCurrentAlbumCardVisible = MutableStateFlow(false)
    val isCurrentAlbumCardVisible: StateFlow<Boolean> = _isCurrentAlbumCardVisible.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistoryRoomEntity>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryRoomEntity>> = _searchHistory.asStateFlow()

    private val albumSongsCache = mutableMapOf<Int, List<Song>>()

    suspend fun loadAlbumSongsCached(albumId: Int): List<Song> {
        return albumSongsCache[albumId] ?: run {
            val songs = songRepository.getSongsByAlbumId(albumId).first()  // collect the flow once
            albumSongsCache[albumId] = songs
            songs
        }
    }

    // This is used in SingleArtistView to track whether the album card is visible on screen, which influences startInMiniMode logic (whether to start in mini mode or full mode when playing a song from the album card).
    fun setCurrentAlbumCardVisible(visible: Boolean) {
        _isCurrentAlbumCardVisible.value = visible
    }

    // This is called from SingleArtistView when an album is selected or a song is played, so the player knows which album card to attach to. Without it, currentAlbumId never updates from null, causing the player to use "album_null" as the card ID, which never matches the album card’s ID ("album_123").
    fun setCurrentAlbumId(albumId: Int?) {
        _currentAlbumId.value = albumId
    }

    fun loadAlbumSongs(albumId: Int) {
        viewModelScope.launch {
            songRepository.getSongsByAlbumId(albumId).collect { songs ->
                _albumSongs.value = songs
            }
        }
    }


    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun setShowBars(show: Boolean) {
        _showBars.value = show
    }

    // Auto‑hide timer logic (called from UI after showing bars)
    fun startAutoHideTimer() {
        viewModelScope.launch {
            if (_isFullscreen.value && _showBars.value) {
                delay(3000)
                _showBars.value = false
            }
        }
    }

    fun handleBackPress(onExit: () -> Unit) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastBackPressTime

        // Double back detected within 500ms → exit regardless
        if (elapsed <= 250) {
            Log.d("BackHandler", "Double back detected, exiting app")
            onExit()
            return
        }

        lastBackPressTime = now

        when {
            _bottomSheetState.value != BottomSheetState.HIDDEN -> {
                setBottomSheetState(BottomSheetState.HIDDEN)
            }
            hasPreviousHistory() -> {
                goBack()
            }
            //else nothing
        }
    }

    val videoArtistsMap: StateFlow<Map<Int, List<Artist>>> = combine(
        artistRepository.getAllArtists(),
        associationRepository.getAllVideoContainsArtists()
    ) { artists: List<Artist>, associations: List<VideoContainsArtist> ->
        associations.groupBy { it.videoId }
            .mapValues { (_, assocs) ->
                assocs.mapNotNull { assoc ->
                    artists.find { it.id == assoc.artistId }
                }
            }
    }.catch { e ->
        Log.e(TAG, "Failed to build video-artists map", e)
        emptyMap<Int, List<Artist>>()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap<Int, List<Artist>>()
    )

    val albumArtistsMap: StateFlow<Map<Int, List<AlbumArtistInfo>>> = combine(
        artistRepository.getAllArtists(),
        associationRepository.getAllAlbumContainsArtists()
    ) { artists: List<Artist>, associations: List<AlbumContainsArtist> ->
        associations.groupBy { it.albumId }
            .mapValues { (_, assocs) ->
                assocs.mapNotNull { assoc ->
                    artists.find { it.id == assoc.artistId }
                        ?.let { AlbumArtistInfo(it, assoc.isMain == 1) }
                }
            }
    }.catch { e ->
        Log.e(TAG, "Failed to build album-artists map", e)
        emptyMap<Int, List<AlbumArtistInfo>>()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

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
        loadSearchHistory()
    }

    // NEW: Load search history from repository
    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.getAllSearchHistory().collect { history ->
                _searchHistory.value = history
            }
        }
    }

    // NEW: Insert a search history entry (called when user performs a search)
    fun addSearchHistoryEntry(query: String, mode: Int, filterPathId: Int) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val entry = SearchHistoryRoomEntity(
                    filterPathId = filterPathId,
                    query = query,
                    mode = mode
                )
                searchHistoryRepository.insertSearchHistory(entry)
                // No need to call loadSearchHistory() because the Flow will auto-update
            }
        }
    }

    // NEW: Delete a single history entry
    fun deleteSearchHistoryEntry(entry: SearchHistoryRoomEntity) {
        viewModelScope.launch {
            searchHistoryRepository.deleteSearchHistory(entry)
            // Flow will update automatically
        }
    }

    // NEW: Clear all search history (optional, could be used in Settings)
    fun clearAllSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.deleteAllSearchHistory()
        }
    }

    fun hasPreviousHistory(): Boolean {
        val entries = enrichedHistory.value
        val currentIndex = entries.indexOfFirst { it.timestamp == currentStateTimestamp }
        return currentIndex != -1 && currentIndex < entries.size - 1
    }

    fun goBack() {
        viewModelScope.launch {
            val entries = enrichedHistory.value
            val currentIndex = entries.indexOfFirst { it.timestamp == currentStateTimestamp }
            if (currentIndex != -1 && currentIndex < entries.size - 1) {
                val previousGroup = entries[currentIndex + 1]  // HistoryGroupItem
                restoreFilterPathFromGroupItem(previousGroup)  // already exists
            }
        }
    }

    private suspend fun enrichFilterPathNames(filters: List<FilterPath>): List<FilterPath> {
        return filters.map { filter ->
            if (filter.entityName.isNotEmpty()) return@map filter
            val name = when (filter.categoryId) {
                FilterPath.CATEGORY_INSTRUMENT -> {
                    instrumentRepository.getInstrumentById(filter.entityId).firstOrNull()?.name ?: ""
                }
                FilterPath.CATEGORY_ARTIST -> {
                    val artist = artistRepository.getArtistById(filter.entityId).firstOrNull()
                    if (artist != null) "${artist.name} ${artist.surname}" else ""
                }
                FilterPath.CATEGORY_DURATION -> {
                    durationRepository.getDurationById(filter.entityId).firstOrNull()?.name ?: ""
                }
                FilterPath.CATEGORY_TYPE -> {
                    typeRepository.getTypeById(filter.entityId).firstOrNull()?.name ?: ""
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
        // Use repository to get instrument count
        val instrumentCount = instrumentRepository.getInstrumentCount()

        println("DEBUG: Database check - Instruments: $instrumentCount")

        // Return true if we have at least some data
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
        Log.d("ShuffleDebug-Album", "🔄 Refresh started at ${System.currentTimeMillis()}")
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
        Log.d("ShuffleDebug-Album", "🔄 Refresh finished")
    }

    private suspend fun checkApiAvailability(): Boolean {
        return try {
            val result = jazzRepository.checkApiConnectivity()
            Log.d("MainVM", "API connectivity check: $result")
            result
        } catch (e: Exception) {
            Log.e("MainVM", "API connectivity exception", e)
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
            val jobs = listOf(
                launch {
                    combine(
                        videoRepository.getAllVideos(),
                        settingsRepository.randomiseVideoList,
                        refreshTrigger
                    ) { videos, shouldRandomise, _ ->
                        if (shouldRandomise) videos.shuffled() else videos
                    }.collect { randomisedVideos ->
                        _uiState.update { it.copy(videos = randomisedVideos) }
                    }
                },
                launch {
                    albumRepository.getAllAlbums().collect { albums ->
                        _uiState.update { it.copy(albums = albums) }
                        Log.d("AlbumDebug", "Initial load: albums = ${albums.size}")
                    }
                },
                launch {
                    instrumentRepository.getAllInstrumentsWithArtistCount().collect { instruments ->
                        _uiState.update {
                            it.copy(
                                allInstruments = instruments,
                                availableInstruments = instruments
                            )
                        }
                        println("DEBUG: Loaded ${instruments.size} instruments")
                    }
                },
                launch {
                    artistRepository.getAllArtistsWithVideoCount().collect { artists ->
                        _uiState.update {
                            it.copy(
                                availableArtists = artists,
                                availableArtistsDisplay = artists
                            )
                        }
                        println("DEBUG: Loaded ${artists.size} artists")
                    }
                },
                launch {
                    typeRepository.getAllTypesWithCount().collect { types ->
                        _uiState.update { it.copy(availableTypes = types) }
                        println("DEBUG: Loaded ${types.size} types")
                    }
                },
                launch {
                    durationRepository.getAllDurationsWithCount().collect { durations ->
                        _uiState.update { it.copy(availableDurations = durations) }
                        println("DEBUG: Loaded ${durations.size} durations")
                    }
                },
                launch {
                    associationRepository.getAllVideoContainsArtists().collect { videoContainsArtists ->
                        _uiState.update { it.copy(availableVideoContainsArtists = videoContainsArtists) }
                        println("DEBUG: Loaded ${videoContainsArtists.size} video-artist associations")
                    }
                },
                launch {
                    associationRepository.getAllAlbumContainsArtists().collect { albumContainsArtists ->
                        _uiState.update { it.copy(availableAlbumContainsArtists = albumContainsArtists) }
                        println("DEBUG: Loaded ${albumContainsArtists.size} album-artist associations")
                    }
                }
            )

            jobs.forEach { it.join() }
            _uiState.update { it.copy(isLoading = false) }
            println("DEBUG: Finished loading all data")
        }
    }

    private fun loadFilterPath() {
        viewModelScope.launch {
            val latestMeta = filterPathRepository.getLatestFilterPathWithMeta()
            if (latestMeta != null) {
                val enriched = enrichFilterPathNames(latestMeta.filters)
                _currentFilterPath.value = enriched
                currentStateTimestamp = latestMeta.timestamp
                applyFiltersFromPath(enriched)
                _currentFilterPathId.value = latestMeta.id
            } else {
                _currentFilterPath.value = emptyList()
                currentStateTimestamp = 0L
                applyFiltersFromPath(emptyList())
                _currentFilterPathId.value = null
            }
            // Load enriched history (used for back navigation and history tab)
            loadEnrichedHistory()
        }
    }

    // Replace filterManager with filterOrchestrator and adapt types
    private fun applyFiltersFromPath(filterPaths: List<FilterPath>) {
        // Cancel previous job to avoid multiple collectors
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            _filterState.update { it.copy(isFiltering = true) }

            combine(
                filterOrchestrator.getFilteredDataFlow(filterPaths),  // was filterManager
                settingsRepository.randomiseVideoList,
                refreshTrigger
            ) { filteredData, shouldRandomise, _ ->
                filteredData to shouldRandomise
            }.collect { (filteredData, shouldRandomise) ->

                val finalVideos = if (shouldRandomise) filteredData.videos.shuffled() else filteredData.videos
                val finalAlbums = if (shouldRandomise) {
                    filteredData.albums.shuffleWithNullThumbnailsAtEnd()
                } else {
                    filteredData.albums
                }
                val displayAlbums = finalAlbums // already shuffled if needed
                val finalArtists = if (shouldRandomise) filteredData.artists.shuffled() else filteredData.artists




                _uiState.update { uiState ->
                    uiState.copy(
                        filteredVideos = finalVideos,
                        filteredAlbums = finalAlbums,
                        availableAlbumsDisplay = displayAlbums,
                        availableArtistsDisplay = finalArtists,
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
            }
        }
    }

    fun handleChipSelection(categoryId: Int, entityId: Int, entityName: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentPath = _currentFilterPath.value
            Log.d("MainViewModel", "Before: currentPath = $currentPath")
            Log.d("MainViewModel", "handleChipSelection: category=$categoryId, entity=$entityId, name=$entityName, selected=$isSelected")

            val newPath = filterOrchestrator.handleChipSelection(currentPath, categoryId, entityId, entityName, isSelected)
            Log.d("MainViewModel", "After orchestrator: newPath = $newPath")

            if (newPath == currentPath) return@launch

            // Delete forward history entries (newer than current state)
            if (currentStateTimestamp > 0L) {
                filterPathRepository.deleteAllNewerThan(currentStateTimestamp)
            }

            // Insert the new path and get its ID
            val newId = filterPathRepository.insertFilterPathAndGetId(newPath)

            // Retrieve the full metadata of the latest path (to get timestamp)
            val latestMeta = filterPathRepository.getLatestFilterPathWithMeta()
            if (latestMeta != null) {
                _currentFilterPathId.value = latestMeta.id
                currentStateTimestamp = latestMeta.timestamp
            } else {
                // Fallback: use the returned ID and current time
                _currentFilterPathId.value = newId.toInt()
                currentStateTimestamp = System.currentTimeMillis()
            }

            // Update UI state
            _currentFilterPath.value = newPath
            applyFiltersFromPath(newPath)
            _filterState.update { it.copy(currentFilterPath = newPath) }


            if (categoryId == FilterPath.CATEGORY_SEARCH && entityName.isNotBlank()) {
                Log.d("MainViewModel", "Adding search chip: query='$entityName', mode=$entityId, filterPathId=${_currentFilterPathId.value}")
                val newFilterPathId = _currentFilterPathId.value
                if (newFilterPathId != null) {
                    addSearchHistoryEntry(entityName, entityId, newFilterPathId)
                }
            }

            // Refresh history
            loadEnrichedHistory()
        }
    }


    private fun clearFilters() {
        viewModelScope.launch {
            // Delete any forward history (entries newer than the current state)
            if (currentStateTimestamp > 0L) {
                filterPathRepository.deleteAllNewerThan(currentStateTimestamp)
            }

            val timestamp = System.currentTimeMillis()
            val insertedId = filterPathRepository.insertFilterPathAndGetId(emptyList())
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
            // Get all history entries from repository (returns List<FilterHistoryEntry>)
            val rawEntries = filterPathRepository.getAllHistoryEntries()


            // Group by filterPathId (each path corresponds to one filter_path row)
            val grouped = rawEntries
                .groupBy { it.filterPathId }
            val result = mutableListOf<HistoryGroupItem>()

            for ((filterPathId, entries) in grouped) {
                val first = entries.first()
                // Deserialize the serialized filter list and enrich names (uses repositories internally)
                val filterPaths = FilterPathMapper.deserialize(first.serialNumber)
                val enrichedPaths = enrichFilterPathNames(filterPaths)

                // Collect videos associated with this filter path (distinct by videoId)
                val videos = entries
                    .filter { it.typeOfMedia == 0 }
                    .mapNotNull { entry ->
                    entry.videoId?.let { videoId ->
                        // Use videoRepository to fetch the full Video domain object
                        videoRepository.getVideoById(videoId).firstOrNull()
                    }
                }.distinctBy { it.id }

                result.add(
                    HistoryGroupItem(
                        filterPathId = filterPathId,
                        timestamp = first.timestamp,
                        filterPaths = enrichedPaths,
                        videos = videos
                    )
                )
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
            filterPathRepository.deleteAll()

            // 2. Insert a new empty filter path (serializes to empty string)
            filterPathRepository.insertFilterPathAndGetId(emptyList())
            val latestMeta = filterPathRepository.getLatestFilterPathWithMeta()
            if (latestMeta != null) {
                _currentFilterPathId.value = latestMeta.id
                currentStateTimestamp = latestMeta.timestamp
            } else {
                // Fallback (should never happen after insertion)
                _currentFilterPathId.value = null
                currentStateTimestamp = System.currentTimeMillis()
            }

            // 3. Reset UI state to empty filters
            _currentFilterPath.value = emptyList()
            applyFiltersFromPath(emptyList())

            // 4. Refresh the history UI (enriched list)
            loadEnrichedHistory()

            // 5. If you still use _historyEntries (raw entities), consider removing it.
            //    The original code collected a flow of Room entities; with the repository,
            //    you would need to expose that flow – but it's better to rely on enrichedHistory.
            //    For now, we skip step 5 to keep the repository boundary clean.

            /*
            _currentFilterPath.value = emptyList()
            currentStateTimestamp = emptyEntry.timestamp
            applyFiltersFromPath(emptyList())
             */

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


    // MainViewModel.kt
    fun refreshHistory() {
        viewModelScope.launch {
            loadEnrichedHistory()   // reload from database
            // Small delay to show the spinner (optional)

        }
    }

    /**
     * Shuffles the list but ensures that any album with a null thumbnail
     * moves to the end, preserving the relative order among null-thumbnail albums.
     */
    fun List<Album>.shuffleWithNullThumbnailsAtEnd(): List<Album> {
        val (withThumb, withoutThumb) = partition { it.getThumbnailUrl() != null }
        return withThumb.shuffled() + withoutThumb
    }

    private suspend fun ensureInitialFilterPath(): Int? {
        val count = filterPathRepository.getCount()
        return if (count == 0) {
            // Insert an empty path (list of filters) – serializes to empty string
            val id = filterPathRepository.insertFilterPathAndGetId(emptyList())
            id.toInt()
        } else {
            filterPathRepository.getLatestFilterPathId()
        }
    }

    // In MainViewModel
    fun triggerShuffle() {
        _isRefreshing.value = true
        refreshTrigger.value += 1
        viewModelScope.launch {
            delay(300)
            _isRefreshing.value = false
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
    val availableAlbumsDisplay: List<Album> = emptyList(),
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


enum class MainTab { VIDEOS, ALBUMS, ARTISTS, HISTORY }