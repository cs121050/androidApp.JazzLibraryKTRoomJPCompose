// PlayerDynamicState.kt
package com.example.jazzlibraryktroomjpcompose.presentation.player

data class PlayerDynamicState(
    val playbackPosition: Long = 0L,
    val videoDuration: Long = 0L,
    val isPlaying: Boolean = false,
)