package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotes",
    foreignKeys = [
        ForeignKey(
            entity = ArtistRoomEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class QuoteRoomEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "quote_id")
    val id: Int,

    @ColumnInfo(name = "quote_text")
    val text: String,

    @ColumnInfo(name = "artist_id")
    val artistId: Int
)