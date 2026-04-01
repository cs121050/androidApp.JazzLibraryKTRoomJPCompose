// PlayerViewModel.kt
package com.example.jazzlibraryktroomjpcompose.presentation.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.player.YouTubePlayerControllerImpl
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.domain.player.VideoPlayerController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: VideoPlayerController
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // Event channel for communication with the UI (MainScreen)
    private val _playerEvents = MutableSharedFlow<PlayerEvent>()
    val playerEvents: SharedFlow<PlayerEvent> = _playerEvents.asSharedFlow()

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
    fun loadVideo(videoId: String,
                  cardId: String?,
                  currentFilterPath: List<FilterPath>?,
                  startInMiniMode: Boolean = false
    ) {
        val videos = _uiState.value.availableVideos ?: return
        val index = videos.indexOfFirst { extractYouTubeVideoId(it.path) == videoId }
        val localId = if (index != -1) videos[index].id else null

        _uiState.update {
            it.copy(
                isVisible = true,
                isInMiniMode = startInMiniMode,
                activeCardId = cardId,
                filterPathAtLoad = currentFilterPath,
                currentIndex = if (index != -1) index else null,
                currentVideoDatabaseId = localId
            )
        }
        playerController.loadVideo(videoId, autoPlay = true)
        Log.d("PlayerViewModel", "loadVideo: startInMiniMode=$startInMiniMode, videoId=$videoId, localId=$localId")
    }

    /**
     * Load a video by its ID, optionally with a filter path.
     * Used when restoring a history entry.
     */
    fun loadVideoById(
        videoId: Int,
        filterPath: List<FilterPath>? = null
    ) {
        // Convert videoId (Int) to String if needed; here we assume it's already a string YouTube ID.
        // In your case, videoId is Int? but in the database it's stored as an ID linking to VideoRoomEntity.
        // We need to retrieve the actual YouTube video ID from the video table.
        // For simplicity, we'll assume you have a method to get the YouTube ID from the video ID.
        // You might need to adjust based on your data model.

        // For now, we'll fetch the video from the database using videoId (if it's the local ID)
        // and then get its YouTube path.
        // We'll add a helper method to the ViewModel to retrieve the video by ID.
        // But to keep it simple, we'll assume videoId is already the YouTube ID string.

    //val videoIdStr =  videoId.toString() // placeholder

        // We need the cardId for animation? When restoring from history, we might not have a card.
        // We'll pass null and the player will start in mini mode.

    //loadVideo(videoIdStr, cardId = null, currentFilterPath = filterPath, startInMiniMode = true)
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
     * Close the player completely (hide it and release resources).
     */
    fun closePlayer() {
        _uiState.update {
            it.copy(
                isVisible = false,
                activeCardId = null,
                currentVideoDatabaseId = null
            )
        }
        playerController.release()
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

    fun updatePlaylist(videos: List<Video>) {
        _uiState.update { it.copy(availableVideos = videos) }
        val currentId = _uiState.value.currentVideoId
        val newIndex = videos.indexOfFirst { extractYouTubeVideoId(it.path) == currentId }
        if (newIndex != -1) {
            val localId = videos[newIndex].id
            _uiState.update {
                it.copy(
                    currentIndex = newIndex,
                    currentVideoDatabaseId = localId
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = null,
                    currentVideoDatabaseId = null
                )
            }
        }
    }

    fun updateCurrentIndex(index: Int) {
        _uiState.update { it.copy(currentIndex = index) }
    }

    fun nextVideo(startInMiniMode: Boolean = false) {
        val state = _uiState.value
        val videos = state.availableVideos ?: return
        val currentIdx = state.currentIndex ?: return
        if (currentIdx + 1 < videos.size) {
            val nextVideo = videos[currentIdx + 1]
            val nextVideoId = extractYouTubeVideoId(nextVideo.path)
            if (nextVideoId != null) {
                loadVideo(nextVideoId, nextVideo.locationId, state.filterPathAtLoad, startInMiniMode)
                _uiState.update { it.copy(currentIndex = currentIdx + 1) }
            }
        }
        Log.d("PlayerViewModel", "nextVideo: startInMiniMode=$startInMiniMode")
    }

    fun previousVideo(startInMiniMode: Boolean = false) {
        val state = _uiState.value
        val videos = state.availableVideos ?: return
        val currentIdx = state.currentIndex ?: return
        if (currentIdx - 1 >= 0) {
            val prevVideo = videos[currentIdx - 1]
            val prevVideoId = extractYouTubeVideoId(prevVideo.path)
            if (prevVideoId != null) {
                loadVideo(prevVideoId, prevVideo.locationId, state.filterPathAtLoad, startInMiniMode)
                _uiState.update { it.copy(currentIndex = currentIdx - 1) }
            }
        }
    }
}