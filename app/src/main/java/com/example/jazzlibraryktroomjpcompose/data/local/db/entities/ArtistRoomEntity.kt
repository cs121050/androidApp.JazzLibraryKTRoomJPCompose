package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    foreignKeys = [
        ForeignKey(
            entity = InstrumentRoomEntity::class,
            parentColumns = ["instrument_id"],
            childColumns = ["instrument_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ArtistRoomEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "artist_id")
    val id: Int,

    @ColumnInfo(name = "artist_name")
    val name: String,

    @ColumnInfo(name = "artist_surname")
    val surname: String,

    @ColumnInfo(name = "instrument_id", defaultValue = "0")
    val instrumentId: Int,

    @ColumnInfo(name = "artist_rank")
    val rank: Int? = 0
)