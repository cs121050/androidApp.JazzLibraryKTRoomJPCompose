package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "filter_path",
    foreignKeys = [
        ForeignKey(
            entity = VideoRoomEntity::class,
            parentColumns = ["video_id"],
            childColumns = ["video_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FilterPathRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "auto_increment_id")
    val autoIncrementId: Int = 0,

    @ColumnInfo(name = "serial_number")
    val serialNumber: String,               // e.g. "I4A156D4G2S1T1"

    @ColumnInfo(name = "video_id")
    val videoId: Int? = null,            // YouTube video ID of the currently playing video

    @ColumnInfo(name = "timestamp")
    val timestamp: Long                     // System.currentTimeMillis()
)