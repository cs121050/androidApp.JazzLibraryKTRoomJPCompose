// PlayerViewModel.kt
    package com.example.jazzlibraryktroomjpcompose.presentation.player
    
    import android.util.Log
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
    import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.FilterPathContainsMediaDao
    import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathContainsMediaRoomEntity
    import com.example.jazzlibraryktroomjpcompose.data.player.YouTubePlayerControllerImpl
    import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
    import com.example.jazzlibraryktroomjpcompose.domain.models.Video
    import com.example.jazzlibraryktroomjpcompose.domain.player.VideoPlayerController
    import com.example.jazzlibraryktroomjpcompose.domain.repository.SongRepository
    import com.example.jazzlibraryktroomjpcompose.ui.main.MainTab
    import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.*
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import java.util.UUID
    
    private const val TAG = "PlayerViewModel"
    
    @HiltViewModel
    class PlayerViewModel @Inject constructor(
        private val playerController: VideoPlayerController,
        private val database: JazzDatabase,
        private val songRepository: SongRepository
    ) : ViewModel() {
    
        private var currentFilterPathId: Int? = null
    
        // UI state
        private val _uiState = MutableStateFlow(PlayerUiState())
        val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
        // Event channel for communication with the UI (MainScreen)
        private val _playerEvents = MutableSharedFlow<PlayerEvent>()
        val playerEvents: SharedFlow<PlayerEvent> = _playerEvents.asSharedFlow()
    
        private val filterPathContainsMediaDao: FilterPathContainsMediaDao
            get() = database.filterPathContainsMediaDao()
    
        val currentVideoDbIdState: StateFlow<Int?> = _uiState
            .map { it.currentVideoDbId }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    
        // Add a new StateFlow
        private val _currentFilterPathMedia = MutableStateFlow<FilterPathContainsMediaRoomEntity?>(null)
        val currentFilterPathMedia: StateFlow<FilterPathContainsMediaRoomEntity?> = _currentFilterPathMedia.asStateFlow()
    
        // ========== 1. Tab change auto‑minimize ==========
        private val _currentTab = MutableStateFlow(MainTab.VIDEOS)
        val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()
    
        fun setCurrentTab(tab: MainTab) {
            _currentTab.value = tab
            // Auto‑minimize player when leaving Videos tab
            if (tab != MainTab.VIDEOS && _uiState.value.isVisible && !_uiState.value.isInMiniMode) {
                minimizePlayer()
            }
        }
    
        // ========== 2. Event when a new video is loaded (for history refresh) ==========
        private val _videoChangedEvent = MutableSharedFlow<Unit>()
        val videoChangedEvent: SharedFlow<Unit> = _videoChangedEvent.asSharedFlow()

        private val _playerSession = MutableStateFlow<PlayerSession?>(null)
        val playerSession: StateFlow<PlayerSession?> = _playerSession.asStateFlow()

        private val _isCardVisible = MutableStateFlow(false)
        private val _globalPlayerVisible = MutableStateFlow(true)

        // Add this with your other event flows
        private val _clearBoundsEvent = MutableSharedFlow<Unit>()
        val clearBoundsEvent: SharedFlow<Unit> = _clearBoundsEvent.asSharedFlow()

        // PlayerViewModel.kt
        private val _isActiveCardVisible = MutableStateFlow(false)
        val isActiveCardVisible: StateFlow<Boolean> = _isActiveCardVisible.asStateFlow()

        fun onCardVisibilityChanged(isVisible: Boolean) {
            _isActiveCardVisible.value = isVisible
            _isCardVisible.value = isVisible
            updateMiniMode()
        }

        fun onGlobalPlayerVisibilityChanged(isVisible: Boolean) {
            _globalPlayerVisible.value = isVisible
            updateMiniMode()
        }

        private fun updateMiniMode() {
            val currentType = _uiState.value.currentTypeOfMedia
            val shouldBeMini = !_globalPlayerVisible.value || !_isCardVisible.value
            if (shouldBeMini && !_uiState.value.isInMiniMode && currentType ==0) {
                minimizePlayer()
            } else if (!shouldBeMini && _uiState.value.isInMiniMode) {
                restoreFullMode()
            }
        }


        init {
            viewModelScope.launch {
                combine(
                    playerController.currentVideoId,
                    playerController.isPlaying,
                    playerController.playbackPosition,
                    playerController.videoDuration
                ) { videoId, isPlaying, position, duration ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentVideoId = videoId,
                            isPlaying = isPlaying,
                            playbackPosition = position,
                            videoDuration = duration
                        )
                    }
                }.launchIn(viewModelScope)
            }
        }
    
        /**
         * Load a video into the player.
         * @param videoId The YouTube video ID.
         * @param cardId The ID of the card that should host the player (if any).
         * @param currentFilterPath The filter path active at the moment of loading.
         */
        /**
         * Load a video into the player.
         */
        fun loadVideo(
            videoId: String,
            cardId: String?,
            currentFilterPath: List<FilterPath>?,
            startInMiniMode: Boolean = false,
            mediaDbId: Int?,
            filterPathId: Int?,
            typeOfMedia: Int?,
            playlist: List<PlaylistItem>? = null,
            startIndex: Int = 0
        ) {
            Log.d(TAG, "loadVideo called, stack trace8888:", Exception())

            viewModelScope.launch {
                _clearBoundsEvent.emit(Unit)
            }
            Log.d(TAG, "loadVideo: videoId=$videoId, typeOfMedia=$typeOfMedia")

            currentFilterPathId = filterPathId

            // Create a session if a playlist is provided
            if (playlist != null && playlist.isNotEmpty()) {
                val session = PlayerSession(
                    sessionId = UUID.randomUUID().toString(),
                    playlist = playlist,
                    currentIndex = startIndex,
                    originalFilterPathId = filterPathId,
                    typeOfMedia = typeOfMedia ?: 0,
                    activeCardId = cardId
                )
                _playerSession.value = session
                _uiState.update { it.copy(currentIndex = startIndex, currentAlbumIndex = startIndex) }
            }

            // Basic UI state
            _uiState.update {
                it.copy(
                    currentVideoDbId = mediaDbId,
                    isVisible = true,
                    isInMiniMode = startInMiniMode,
                    activeCardId = cardId,
                    filterPathAtLoad = currentFilterPath,
                    currentTypeOfMedia = typeOfMedia ?: 0
                )
            }

            _isCardVisible.value = !startInMiniMode

            playerController.loadVideo(videoId, autoPlay = true)
            Log.d(TAG, "playback started for videoId=$videoId")
    
            // Save to history (filter_path_contains_media)
            if (mediaDbId != null && filterPathId != null && filterPathId > 0) {
                viewModelScope.launch {
                    val entry = FilterPathContainsMediaRoomEntity(
                        filterPathId = filterPathId,
                        videoId = mediaDbId,
                        typeOfMedia = typeOfMedia
                    )
                    filterPathContainsMediaDao.insert(entry)
                    _currentFilterPathMedia.value = entry
                    Log.d(TAG, "Saved to history: filterPathId=$filterPathId, mediaDbId=$mediaDbId, type=$typeOfMedia")
                }
            }
    
            // Emit event to refresh history in UI
            viewModelScope.launch {
                _videoChangedEvent.emit(Unit)
            }
        }
        /**
         * Called by the UI when a card's visibility changes.
         * If the active card becomes completely invisible, switch to mini‑player mode.
         * If it becomes visible again, switch back to card‑attached mode.
         */
        fun onCardVisibilityChanged(cardId: String, isVisible: Boolean) {
            val current = _uiState.value
            // Only react if this is the active card
            if (cardId == current.activeCardId) {
                _uiState.update { it.copy(isInMiniMode = !isVisible) }
            }
        }
    
        /**
         * User tapped the right corner of the mini‑player.
         * This should scroll the list to the active card if it exists, otherwise restore the original filter path.
         */
        fun onMiniPlayerRightTap() {
            val current = _uiState.value
            val activeCardId = current.activeCardId
            if (activeCardId != null) {
                // Request scrolling to the card (UI will handle actual scroll)
                viewModelScope.launch {
                    _playerEvents.emit(PlayerEvent.ScrollToCard(activeCardId))
                }
            } else if (!current.filterPathAtLoad.isNullOrEmpty()) {
                // No active card – restore the original filter path to bring back the video's context
                viewModelScope.launch {
                    _playerEvents.emit(PlayerEvent.RestoreFilterPath(current.filterPathAtLoad))
                }
            }
            // If no filter path either, do nothing (maybe video was loaded directly)
        }
    
        /**
         * User tapped the left corner of the mini‑player (play/pause).
         */
        fun onMiniPlayerLeftTap() {
            if (_uiState.value.isPlaying) {
                playerController.pause()
            } else {
                playerController.play()
            }
        }
    
        /**
         * User tapped the center of the mini‑player (go fullscreen).
         */
        fun onMiniPlayerCenterTap() {
            viewModelScope.launch {
                _playerEvents.emit(PlayerEvent.RequestFullScreen)
            }
            // Also tell controller to enter fullscreen mode (will be implemented later)
            playerController.setFullScreen(true)
        }
    
        /**
         * Called when the player successfully moves back to a card after scrolling.
         * This updates the UI state.
         */
        fun onPlayerMovedToCard() {
            _uiState.update { it.copy(isInMiniMode = false) }
        }
    
        /**
         * Called when the player moves to mini‑player mode (e.g., after card scrolls off‑screen).
         */
        fun onPlayerMovedToMini() {
            _uiState.update { it.copy(isInMiniMode = true) }
        }
    
        /**
         * Clear the stored filter path (e.g., after it has been restored).
         */
        fun clearStoredFilterPath() {
            _uiState.update { it.copy(filterPathAtLoad = null) }
        }
    
        override fun onCleared() {
            playerController.release()
            super.onCleared()
        }
    
        fun setPlayer(youTubePlayer: YouTubePlayer) {
            viewModelScope.launch {
                // Since setPlayer is only in the implementation (not in the interface),
                // we cast to the concrete type. Alternatively, we could add it to the interface.
                (playerController as? YouTubePlayerControllerImpl)?.setPlayer(youTubePlayer)
            }
        }
        /**
         * Switch the player to mini‑mode (small floating box in the corner).
         * This is typically called when the active video card scrolls out of view.
         */
        fun minimizePlayer() {
            _uiState.update { it.copy(isInMiniMode = true) }
        }
    
        fun restoreFullMode() {
            _uiState.update { it.copy(isInMiniMode = false) }
        }
    
    
        fun seekTo(positionMs: Long) {
            playerController.seekTo(positionMs)
        }
    
        fun rewind10Seconds() {
            val newPosition = (_uiState.value.playbackPosition - 10000).coerceAtLeast(0)
            playerController.seekTo(newPosition)
        }
    
        fun forward10Seconds() {
            // You'll need to track video duration - add it to PlayerUiState
            val newPosition = (_uiState.value.playbackPosition + 10000)
                .coerceAtMost(_uiState.value.videoDuration)
            playerController.seekTo(newPosition)
        }
    
        private fun extractYouTubeVideoId(url: String): String? {
            val pattern = "(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([a-zA-Z0-9_-]{11})"
            val regex = Regex(pattern)
            return regex.find(url)?.groupValues?.get(1)
        }

        fun nextVideo(startInMiniMode: Boolean = false) {
            val session = _playerSession.value ?: run {
                Log.w(TAG, "nextVideo called but no active session")
                return
            }
            val newIndex = session.currentIndex + 1
            if (newIndex >= session.playlist.size) {
                Log.d(TAG, "Already at last item")
                return
            }
            val item = session.playlist[newIndex]

            // Update session index first
            _playerSession.update { it?.copy(currentIndex = newIndex) }

            when (item) {
                is PlaylistItem.VideoItem -> {
                    val video = item.video
                    val videoId = extractYouTubeVideoId(video.path) ?: return
                    loadVideo(
                        videoId = videoId,
                        cardId = video.locationId,
                        currentFilterPath = null,
                        startInMiniMode = startInMiniMode,
                        mediaDbId = video.id,
                        filterPathId = session.originalFilterPathId,
                        typeOfMedia = 0,
                        playlist = session.playlist,
                        startIndex = newIndex
                    )
                }
                is PlaylistItem.SongItem -> {
                    val song = item.song
                    val videoId = song.ytVideoId ?: return
                    loadVideo(
                        videoId = videoId,
                        cardId = "album_${item.albumId}",
                        currentFilterPath = null,
                        startInMiniMode = startInMiniMode,
                        mediaDbId = song.songId,
                        filterPathId = session.originalFilterPathId,
                        typeOfMedia = 1,
                        playlist = session.playlist,
                        startIndex = newIndex
                    )
                }
            }
        }

        fun previousVideo(startInMiniMode: Boolean = false) {
            val session = _playerSession.value ?: run {
                Log.w(TAG, "previousVideo called but no active session")
                return
            }
            val newIndex = session.currentIndex - 1
            if (newIndex < 0) {
                Log.d(TAG, "Already at first item")
                return
            }
            val item = session.playlist[newIndex]

            _playerSession.update { it?.copy(currentIndex = newIndex) }

            when (item) {
                is PlaylistItem.VideoItem -> {
                    val video = item.video
                    val videoId = extractYouTubeVideoId(video.path) ?: return
                    loadVideo(
                        videoId = videoId,
                        cardId = video.locationId,
                        currentFilterPath = null,
                        startInMiniMode = startInMiniMode,
                        mediaDbId = video.id,
                        filterPathId = session.originalFilterPathId,
                        typeOfMedia = 0,
                        playlist = session.playlist,
                        startIndex = newIndex
                    )
                }
                is PlaylistItem.SongItem -> {
                    val song = item.song
                    val videoId = song.ytVideoId ?: return
                    loadVideo(
                        videoId = videoId,
                        cardId = "album_${item.albumId}",
                        currentFilterPath = null,
                        startInMiniMode = startInMiniMode,
                        mediaDbId = song.songId,
                        filterPathId = session.originalFilterPathId,
                        typeOfMedia = 1,
                        playlist = session.playlist,
                        startIndex = newIndex
                    )
                }
            }
        }


        fun closePlayer(filterPathId: Int?) {
            _playerSession.value = null          // Discard retained playlist
            _uiState.update {
                it.copy(
                    isVisible = false,
                    activeCardId = null,
                    currentTypeOfMedia = null,
                    currentAlbumId = null,
                    currentAlbumSongs = emptyList(),
                    currentAlbumIndex = null
                )
            }
            if (filterPathId != null) {
                viewModelScope.launch {
                    val entry = FilterPathContainsMediaRoomEntity(
                        filterPathId = filterPathId,
                        videoId = null,
                        typeOfMedia = null
                    )
                    filterPathContainsMediaDao.insert(entry)
                    _currentFilterPathMedia.value = entry
                    Log.d(TAG, "Inserted null entry for filterPathId=$filterPathId")
                }
            }
            playerController.release()
        }
    }