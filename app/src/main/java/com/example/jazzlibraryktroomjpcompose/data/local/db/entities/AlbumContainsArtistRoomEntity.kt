package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "album_contains_artist",
    primaryKeys = ["artist_id", "album_id"],
    foreignKeys = [
        ForeignKey(
            entity = ArtistRoomEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlbumRoomEntity::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlbumContainsArtistRoomEntity(
    @ColumnInfo(name = "artist_id")
    val artistId: Int,

    @ColumnInfo(name = "album_id")
    val albumId: Int,

    @ColumnInfo(name = "is_main")
    val isMain: Int     // 1 = true, 0 = false (mirrors API int)
)