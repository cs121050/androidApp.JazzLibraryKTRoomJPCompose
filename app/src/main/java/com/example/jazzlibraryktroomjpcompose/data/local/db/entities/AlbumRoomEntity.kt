package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: Int,

    @ColumnInfo(name = "youtube_video_id_for_thumbnail")
    val youtubeVideoIdForThumbnail: String?,

    @ColumnInfo(name = "rating_average")
    val ratingAverage: Double?,

    @ColumnInfo(name = "rating_count")
    val ratingCount: Int?,

    @ColumnInfo(name = "released")
    val released: String?,

    @ColumnInfo(name = "release_type")
    val releaseType: String?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "wikipedia_url")
    val wikipediaUrl: String?,

    @ColumnInfo(name = "coverartarchive_thumb")
    val coverartarchiveThumb: String?,

    @ColumnInfo(name = "extra_artists")
    val extraArtists: String?,   // JSON array

    @ColumnInfo(name = "genres")
    val genres: String?,         // JSON array

    @ColumnInfo(name = "labels")
    val labels: String?,         // JSON array

    @ColumnInfo(name = "styles")
    val styles: String?,         // JSON array

    @ColumnInfo(name = "tracklist")
    val tracklist: String?,      // JSON array

    @ColumnInfo(name = "wikipedia_data")
    val wikipediaData: String?   // JSON array
)