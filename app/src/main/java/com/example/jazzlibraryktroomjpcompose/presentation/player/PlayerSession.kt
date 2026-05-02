package com.example.jazzlibraryktroomjpcompose.presentation.player

data class PlayerSession(
    val sessionId: String,                     // unique ID for this session
    val playlist: List<PlaylistItem>,          // the retained list (videos or songs)
    val currentIndex: Int,                     // current position in playlist
    val originalFilterPathId: Int?,            // for history tracking
    val typeOfMedia: Int,                      // 0 = educational, 1 = album
    val activeCardId: String?                  // ID of the card hosting the player
)