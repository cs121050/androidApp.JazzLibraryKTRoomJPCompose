package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_path")
data class FilterPathRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "auto_increment_id")
    val autoIncrementId: Int = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Int, // 1=instrument, 2=artist, 3=duration, 4=type

    @ColumnInfo(name = "entity_id")
    val entityId: Int,

    @ColumnInfo(name = "entity_name")
    val entityName: String
)