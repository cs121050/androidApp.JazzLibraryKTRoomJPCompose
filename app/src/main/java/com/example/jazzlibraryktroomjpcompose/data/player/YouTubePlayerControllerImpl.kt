package com.example.jazzlibraryktroomjpcompose.data.player

import com.example.jazzlibraryktroomjpcompose.domain.player.PlayerState
import com.example.jazzlibraryktroomjpcompose.domain.player.VideoPlayerController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton // will be replaced with @ActivityRetainedScoped via module

class YouTubePlayerControllerImpl @Inject constructor(
    private val coroutineScope: CoroutineScope // we'll inject this from module
) : VideoPlayerController {

    private val _currentVideoId = MutableStateFlow<String?>(null)
    override val currentVideoId: StateFlow<String?> = _currentVideoId

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackPosition = MutableStateFlow(0L)
    override val playbackPosition: StateFlow<Long> = _playbackPosition

    private val _playerState = MutableStateFlow(PlayerState.UNINITIALIZED)
    override val playerState: StateFlow<PlayerState> = _playerState

    private var youTubePlayer: YouTubePlayer? = null
    private var currentListener: AbstractYouTubePlayerListener? = null

    // SharedFlow with replay 1 – new collectors get the latest player immediately
    private val _playerReady = MutableSharedFlow<YouTubePlayer>(replay = 1, extraBufferCapacity = 0)
    private suspend fun awaitPlayer(): YouTubePlayer = _playerReady.first()

    private var savedPosition: Long = 0L
    private var wasPlaying: Boolean = false

    override fun loadVideo(videoId: String, autoPlay: Boolean) {
        _currentVideoId.value = videoId
        _playerState.value = PlayerState.BUFFERING
        coroutineScope.launch {
            val player = awaitPlayer()
            if (autoPlay) {
                player.loadVideo(videoId, 0f)
            } else {
                player.cueVideo(videoId, 0f)
            }
        }
    }

    override fun play() {
        coroutineScope.launch {
            awaitPlayer().play()
            _isPlaying.value = true
            _playerState.value = PlayerState.PLAYING
        }
    }

    override fun pause() {
        coroutineScope.launch {
            awaitPlayer().pause()
            _isPlaying.value = false
            _playerState.value = PlayerState.PAUSED
        }
    }

    override fun seekTo(positionMs: Long) {
        coroutineScope.launch {
            awaitPlayer().seekTo(positionMs.toFloat() / 1000f)
            _playbackPosition.value = positionMs
        }
    }

    override fun release() {
        youTubePlayer = null
        // If you need to clear the player reference, do so.
        // The view itself is released by the UI.
    }

    override fun prepareForMove() {
        savedPosition = _playbackPosition.value
        wasPlaying = _isPlaying.value
        if (wasPlaying) {
            pause()
        }
    }

    override fun afterMoveRestore() {
        coroutineScope.launch {
            val player = awaitPlayer()
            if (savedPosition > 0) {
                player.seekTo(savedPosition.toFloat() / 1000f)
            }
            if (wasPlaying) {
                player.play()
                _isPlaying.value = true
                _playerState.value = PlayerState.PLAYING
            }
        }
    }

    override fun setFullScreen(fullScreen: Boolean) {
        // placeholder
    }

    /**
     * Called by the UI when the YouTubePlayerView is ready.
     */
    suspend fun setPlayer(youTubePlayer: YouTubePlayer) {
        // Remove previous listener if any
        currentListener?.let { oldListener ->
            this.youTubePlayer?.removeListener(oldListener)
        }
        this.youTubePlayer = youTubePlayer

        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                _playerState.value = PlayerState.READY
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                _playerState.value = when (state) {
                    PlayerConstants.PlayerState.UNSTARTED -> PlayerState.UNINITIALIZED
                    PlayerConstants.PlayerState.ENDED -> PlayerState.ENDED
                    PlayerConstants.PlayerState.PLAYING -> PlayerState.PLAYING
                    PlayerConstants.PlayerState.PAUSED -> PlayerState.PAUSED
                    PlayerConstants.PlayerState.BUFFERING -> PlayerState.BUFFERING
                    PlayerConstants.PlayerState.VIDEO_CUED -> PlayerState.READY
                    else -> PlayerState.UNINITIALIZED
                }
                _isPlaying.value = state == PlayerConstants.PlayerState.PLAYING
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                _playbackPosition.value = (second * 1000).toLong()
            }
        }
        youTubePlayer.addListener(listener)
        currentListener = listener

        // Emit the new player instance to the flow
        _playerReady.emit(youTubePlayer)
    }
}