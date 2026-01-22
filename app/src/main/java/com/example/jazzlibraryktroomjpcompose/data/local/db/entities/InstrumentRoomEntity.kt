package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instruments")
data class InstrumentRoomEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "instrument_id")
    val id: Int,

    @ColumnInfo(name = "instrument_name")
    val name: String
)