package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    foreignKeys = [
        ForeignKey(
            entity = FilterPathRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["filter_path_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SearchHistoryRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "filter_path_id")
    val filterPathId: Int,          // Links to the filter_path that contains the search chip

    @ColumnInfo(name = "query")
    val query: String,

    @ColumnInfo(name = "mode")
    val mode: Int,                  // 0 = video, 1 = artist, 2 = album

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)