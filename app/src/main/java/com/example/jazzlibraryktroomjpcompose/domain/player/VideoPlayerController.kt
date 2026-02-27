package com.example.jazzlibraryktroomjpcompose.domain.player

import kotlinx.coroutines.flow.StateFlow

/**
 * Single, activity‑scoped controller for the YouTube player.
 * All playback commands and state observation go through this interface.
 */
interface VideoPlayerController {
    // Observable state
    val currentVideoId: StateFlow<String?>
    val isPlaying: StateFlow<Boolean>
    val playbackPosition: StateFlow<Long>          // in milliseconds
    val playerState: StateFlow<PlayerState>
    val videoDuration: StateFlow<Long>

    // Playback control
    fun loadVideo(videoId: String, autoPlay: Boolean = true)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()

    // Reparenting support (called by the UI when moving the player overlay)
    fun prepareForMove()   // e.g., save position, pause if needed
    fun afterMoveRestore() // e.g., restore position and play state

    // Optional – will be used later for full‑screen expansion
    fun setFullScreen(fullScreen: Boolean)
}