package com.example.jazzlibraryktroomjpcompose.data.local.db.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "video_contains_artist",
    primaryKeys = ["artist_id", "video_id"],
    foreignKeys = [
        ForeignKey(
            entity = ArtistRoomEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
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
data class VideoContainsArtistRoomEntity(
    @ColumnInfo(name = "artist_id")
    val artistId: Int,

    @ColumnInfo(name = "video_id")
    val videoId: Int
)