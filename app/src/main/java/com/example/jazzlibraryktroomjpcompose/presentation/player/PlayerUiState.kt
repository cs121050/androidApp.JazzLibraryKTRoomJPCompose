package com.example.jazzlibraryktroomjpcompose.presentation.player


import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

data class PlayerUiState(
    val isVisible: Boolean = false,
    val isInMiniMode: Boolean = false,
    val currentVideoId: String? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val videoDuration: Long = 0L,  // Add this
    val activeCardId: String? = null,
    val filterPathAtLoad: List<FilterPath>? = null,
    val playerInstanceId: Int = 0
)