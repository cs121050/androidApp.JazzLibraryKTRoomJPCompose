package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class DurationWithVideoCount(
    @Embedded
    val duration: DurationRoomEntity,
    @ColumnInfo(name = "video_count")
    val videoCount: Int
)