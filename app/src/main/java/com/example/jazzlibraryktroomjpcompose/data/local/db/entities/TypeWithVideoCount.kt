package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class TypeWithVideoCount(
    @Embedded
    val type: TypeRoomEntity,
    @ColumnInfo(name = "video_count")
    val videoCount: Int
)