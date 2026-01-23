package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    foreignKeys = [
        ForeignKey(
            entity = DurationRoomEntity::class,
            parentColumns = ["duration_id"],
            childColumns = ["duration_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TypeRoomEntity::class,
            parentColumns = ["type_id"],
            childColumns = ["type_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class VideoRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "video_id")
    val id: Int,

    @ColumnInfo(name = "video_name")
    val name: String,

    @ColumnInfo(name = "video_duration")
    val duration: String,

    @ColumnInfo(name = "video_path")
    val path: String,

    @ColumnInfo(name = "location_id")
    val locationId: String,

    @ColumnInfo(name = "video_availability")
    val availability: String,

    @ColumnInfo(name = "duration_id", defaultValue = "0")
    val durationId: Int,

    @ColumnInfo(name = "type_id", defaultValue = "0")
    val typeId: Int
)