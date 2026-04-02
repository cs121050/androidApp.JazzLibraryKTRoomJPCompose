package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "filter_path_contains_video",
    foreignKeys = [
        ForeignKey(
            entity = FilterPathRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["filter_path_room_entity_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VideoRoomEntity::class,
            parentColumns = ["video_id"],
            childColumns = ["video_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FilterPathContainsVideoRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "filter_path_room_entity_id")
    val filterPathId: Int,

    @ColumnInfo(name = "video_id")
    val videoId: Int
)