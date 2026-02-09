package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class InstrumentWithVideoCount(
    @Embedded
    val instrument: InstrumentRoomEntity,
    @ColumnInfo(name = "video_count") //THAT IS ARTIST COUNT
    val videoCount: Int
)