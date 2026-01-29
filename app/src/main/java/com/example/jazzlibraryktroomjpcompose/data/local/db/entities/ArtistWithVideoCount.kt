package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ArtistWithVideoCount(
    @Embedded
    val artist: ArtistRoomEntity,
    @ColumnInfo(name = "video_count")
    val videoCount: Int
)