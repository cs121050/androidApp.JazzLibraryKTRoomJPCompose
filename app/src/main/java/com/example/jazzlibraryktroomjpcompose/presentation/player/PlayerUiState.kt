package com.example.jazzlibraryktroomjpcompose.presentation.player

import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

data class PlayerUiState(
    val isVisible: Boolean = false,               // Whether the player should be shown at all
    val isInMiniMode: Boolean = false,            // True = in mini‑player, false = attached to a card
    val currentVideoId: String? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val activeCardId: String? = null,             // ID of the video card currently associated with the player
    val filterPathAtLoad: List<FilterPath>? = null // Stored filter path when video was loaded
)