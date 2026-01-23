package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "durations")
data class DurationRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "duration_id")
    val id: Int,

    @ColumnInfo(name = "duration_name")
    val name: String,

    @ColumnInfo(name = "duration_description")
    val description: String
)