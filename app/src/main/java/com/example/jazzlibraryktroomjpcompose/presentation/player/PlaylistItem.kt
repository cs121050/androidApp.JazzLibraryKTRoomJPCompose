package com.example.jazzlibraryktroomjpcompose.presentation.player

import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import com.example.jazzlibraryktroomjpcompose.domain.models.Video

sealed class PlaylistItem {
    data class VideoItem(val video: Video) : PlaylistItem()
    data class SongItem(val song: Song, val albumId: Int) : PlaylistItem()
}