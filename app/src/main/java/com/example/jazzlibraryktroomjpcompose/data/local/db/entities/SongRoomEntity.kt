package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    foreignKeys = [
        ForeignKey(
            entity = ArtistRoomEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["main_artist_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlbumRoomEntity::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class SongRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Int,

    @ColumnInfo(name = "main_artist_id")
    val mainArtistId: Int,

    @ColumnInfo(name = "related_artists")
    val relatedArtists: String?,   // comma-separated artist IDs or names

    @ColumnInfo(name = "album_id")
    val albumId: Int?,

    @ColumnInfo(name = "song_title")
    val songTitle: String?,

    @ColumnInfo(name = "duration")
    val duration: String?,

    @ColumnInfo(name = "yt_videoid")
    val ytVideoId: String?,

    @ColumnInfo(name = "video_availability")
    val videoAvailability: String?
)