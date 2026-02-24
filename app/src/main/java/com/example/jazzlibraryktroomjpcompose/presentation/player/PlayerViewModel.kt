// PlayerViewModel.kt
package com.example.jazzlibraryktroomjpcompose.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.player.YouTubePlayerControllerImpl
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
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
        // Sync with controller's flows to keep playback state updated
        viewModelScope.launch {
            combine(
                playerController.currentVideoId,
                playerController.isPlaying,
                playerController.playbackPosition
            ) { videoId, isPlaying, position ->
                // Update only the playback-related fields, preserve others
                _uiState.update { currentState ->
                    currentState.copy(
                        currentVideoId = videoId,
                        isPlaying = isPlaying,
                        playbackPosition = position
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
    fun loadVideo(videoId: String, cardId: String?, currentFilterPath: List<FilterPath>?) {
        _uiState.update {
            it.copy(
                isVisible = true,
                isInMiniMode = false,          // Initially attached to card if cardId is provided
                activeCardId = cardId,
                filterPathAtLoad = currentFilterPath
            )
        }
        playerController.loadVideo(videoId, autoPlay = true)
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
        _uiState.update { it.copy(isVisible = false, activeCardId = null) }
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
}