// PlayerStableState.kt
package com.example.jazzlibraryktroomjpcompose.presentation.player

data class PlayerStableState(
    val isVisible: Boolean = false,
    val isInMiniMode: Boolean = false,
    val activeCardId: String? = null,
    val currentVideoId: String? = null,
    val currentTypeOfMedia: Int? = null,      // 0 = video, 1 = album/song
    val currentVideoDbId: Int? = null,
    val currentMediaEntryTypeOfMedia: Int? = null,
    val playerInstanceId: Int = 0,             // or String, used as key for YouTubePlayerView
)