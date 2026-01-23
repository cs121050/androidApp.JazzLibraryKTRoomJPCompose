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
            onDelete = ForeignKey.SET_NULL // Changed to SET_NULL since artist_id can be null
        ),
        ForeignKey(
            entity = VideoRoomEntity::class,
            parentColumns = ["video_id"],
            childColumns = ["video_id"],
            onDelete = ForeignKey.SET_NULL
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
    val artistId: Int?, // Made nullable

    @ColumnInfo(name = "video_id")
    val videoId: Int? = null
)