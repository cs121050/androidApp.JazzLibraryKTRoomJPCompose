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
    val rank: Int? = 0,

    @ColumnInfo(name = "spotify_playlist_id")
    val spotifyPlaylistId: String?,

    @ColumnInfo(name = "musicbrainz_uuid")
    val musicbrainzUUID: String?,

    @ColumnInfo(name = "discogs_id")
    val discogsId: Int? = 0,

    @ColumnInfo(name = "wikipedia_url")
    val wikipediaUrl: String? = null,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,

    @ColumnInfo(name = "image_author")
    val imageAuthor: String? = null,

    @ColumnInfo(name = "image_license")
    val imageLicense: String? = null,

    @ColumnInfo(name = "image_source_url")
    val imageSourceUrl: String? = null,

    @ColumnInfo(name = "wikipedia_data")
    val wikipediaData: String? = null,

    @ColumnInfo(name = "embedable_video_count", defaultValue = "0")
    val embedableVideoCount: Int = 0
)




