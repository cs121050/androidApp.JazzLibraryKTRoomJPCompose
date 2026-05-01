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
        typeOfMedia: Int?
    ) {
        Log.d(TAG, "loadVideo: videoId=$videoId, typeOfMedia=$typeOfMedia, mediaDbId=$mediaDbId, startInMiniMode=$startInMiniMode")

        currentFilterPathId = filterPathId

        // --- Basic UI state (visibility, card binding) ---
        _uiState.update {
            it.copy(
                currentVideoDbId = mediaDbId,
                isVisible = true,
                isInMiniMode = startInMiniMode,
                activeCardId = cardId,
                filterPathAtLoad = currentFilterPath
            )
        }

        // --- Handle album vs educational media ---
        when (typeOfMedia) {
            1 -> {
                // ✅ IMPORTANT: Set album mode IMMEDIATELY (synchronously)
                _uiState.update { it.copy(currentTypeOfMedia = 1) }
                Log.d(TAG, "Album mode activated (currentTypeOfMedia = 1)")

                // Load album songs in background
                if (mediaDbId != null) {
                    viewModelScope.launch {
                        Log.d(TAG, "Loading album songs for mediaDbId=$mediaDbId")
                        val song = songRepository.getSongById(mediaDbId).firstOrNull()
                        val albumId = song?.albumId
                        if (albumId != null) {
                            val songs = songRepository.getSongsByAlbumId(albumId).firstOrNull() ?: emptyList()
                            val idx = songs.indexOfFirst { it.songId == mediaDbId }
                            _uiState.update {
                                it.copy(
                                    currentAlbumId = albumId,
                                    currentAlbumSongs = songs,
                                    currentAlbumIndex = if (idx != -1) idx else null
                                )
                            }
                            Log.d(TAG, "Album loaded: albumId=$albumId, songs count=${songs.size}, index=$idx")
                        } else {
                            Log.w(TAG, "No album found for songId=$mediaDbId, falling back to educational mode")
                            _uiState.update { it.copy(currentTypeOfMedia = 0) }
                        }
                    }
                } else {
                    Log.w(TAG, "mediaDbId is null for album mode, keeping currentTypeOfMedia=1 without songs")
                }
            }
            else -> {
                // Educational mode (0 or null)
                _uiState.update {
                    it.copy(
                        currentTypeOfMedia = 0,
                        currentAlbumId = null,
                        currentAlbumSongs = emptyList(),
                        currentAlbumIndex = null
                    )
                }
                Log.d(TAG, "Educational mode (currentTypeOfMedia = 0)")

                // For educational videos, set index inside availableVideos
                val videos = _uiState.value.availableVideos
                if (videos != null) {
                    val idx = videos.indexOfFirst { extractYouTubeVideoId(it.path) == videoId }
                    if (idx != -1) {
                        _uiState.update { it.copy(currentIndex = idx) }
                        Log.d(TAG, "Set educational index = $idx")
                    }
                }
            }
        }

        // Start playback
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

    // Keep track of the playlist (called from MainScreen)
    fun updatePlaylist(videos: List<Video>) {
        _uiState.update { it.copy(availableVideos = videos) }
        // Update current index based on the currently loaded video
        val currentId = _uiState.value.currentVideoId
        val newIndex = videos.indexOfFirst { extractYouTubeVideoId(it.path) == currentId }
        if (newIndex != -1) {
            _uiState.update { it.copy(currentIndex = newIndex) }
        } else {
            _uiState.update { it.copy(currentIndex = null) }
        }
    }

    fun updateCurrentIndex(index: Int) {
        _uiState.update { it.copy(currentIndex = index) }
    }

    fun nextVideo(startInMiniMode: Boolean = false) {
        val state = _uiState.value
        Log.d(TAG, "nextVideo called, currentTypeOfMedia=${state.currentTypeOfMedia}")
        when (state.currentTypeOfMedia) {
            1 -> {  // Album mode
                val songs = state.currentAlbumSongs
                if (songs.isEmpty()) {
                    Log.w(TAG, "Album mode but song list empty, cannot go next")
                    return
                }
                val currentIdx = state.currentAlbumIndex ?: run {
                    Log.w(TAG, "Album mode but currentAlbumIndex is null")
                    return
                }
                if (currentIdx + 1 < songs.size) {
                    val nextSong = songs[currentIdx + 1]
                    nextSong.ytVideoId?.let { videoId ->
                        Log.d(TAG, "Album next: playing song ${nextSong.songTitle} (id=${nextSong.songId})")
                        loadVideo(
                            videoId = videoId,
                            cardId = "album_${state.currentAlbumId ?: return}",
                            currentFilterPath = state.filterPathAtLoad,
                            startInMiniMode = startInMiniMode,
                            mediaDbId = nextSong.songId,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 1
                        )
                    } ?: Log.e(TAG, "Next song has no ytVideoId")
                } else {
                    Log.d(TAG, "Already at last song of album")
                }
            }
            0 -> {  // Educational mode
                val videos = state.availableVideos ?: run {
                    Log.w(TAG, "Educational mode but availableVideos is null")
                    return
                }
                val currentIdx = state.currentIndex ?: run {
                    Log.w(TAG, "Educational mode but currentIndex is null")
                    return
                }
                if (currentIdx + 1 < videos.size) {
                    val nextVideo = videos[currentIdx + 1]
                    val nextVideoId = extractYouTubeVideoId(nextVideo.path)
                    if (nextVideoId != null) {
                        Log.d(TAG, "Educational next: playing video ${nextVideo.name} (id=${nextVideo.id})")
                        loadVideo(
                            videoId = nextVideoId,
                            cardId = nextVideo.locationId,
                            currentFilterPath = state.filterPathAtLoad,
                            startInMiniMode = startInMiniMode,
                            mediaDbId = nextVideo.id,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 0
                        )
                    } else {
                        Log.e(TAG, "Next educational video has no YouTube ID")
                    }
                } else {
                    Log.d(TAG, "Already at last educational video")
                }
            }
            else -> Log.d(TAG, "No media type active, ignoring next")
        }
    }

    fun previousVideo(startInMiniMode: Boolean = false) {
        val state = _uiState.value
        Log.d(TAG, "previousVideo called, currentTypeOfMedia=${state.currentTypeOfMedia}")
        when (state.currentTypeOfMedia) {
            1 -> {
                val songs = state.currentAlbumSongs
                if (songs.isEmpty()) {
                    Log.w(TAG, "Album mode but song list empty, cannot go previous")
                    return
                }
                val currentIdx = state.currentAlbumIndex ?: return
                if (currentIdx - 1 >= 0) {
                    val prevSong = songs[currentIdx - 1]
                    prevSong.ytVideoId?.let { videoId ->
                        Log.d(TAG, "Album previous: playing song ${prevSong.songTitle} (id=${prevSong.songId})")
                        loadVideo(
                            videoId = videoId,
                            cardId = "album_${state.currentAlbumId ?: return}",
                            currentFilterPath = state.filterPathAtLoad,
                            startInMiniMode = startInMiniMode,
                            mediaDbId = prevSong.songId,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 1
                        )
                    } ?: Log.e(TAG, "Previous song has no ytVideoId")
                } else {
                    Log.d(TAG, "Already at first song of album")
                }
            }
            0 -> {
                val videos = state.availableVideos ?: return
                val currentIdx = state.currentIndex ?: return
                if (currentIdx - 1 >= 0) {
                    val prevVideo = videos[currentIdx - 1]
                    val prevVideoId = extractYouTubeVideoId(prevVideo.path)
                    if (prevVideoId != null) {
                        Log.d(TAG, "Educational previous: playing video ${prevVideo.name} (id=${prevVideo.id})")
                        loadVideo(
                            videoId = prevVideoId,
                            cardId = prevVideo.locationId,
                            currentFilterPath = state.filterPathAtLoad,
                            startInMiniMode = startInMiniMode,
                            mediaDbId = prevVideo.id,
                            filterPathId = currentFilterPathId,
                            typeOfMedia = 0
                        )
                    } else {
                        Log.e(TAG, "Previous educational video has no YouTube ID")
                    }
                } else {
                    Log.d(TAG, "Already at first educational video")
                }
            }
            else -> Log.d(TAG, "No media type active, ignoring previous")
        }
    }

    fun closePlayer(filterPathId: Int?) {
        Log.d(TAG, "closePlayer called, filterPathId=$filterPathId")
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