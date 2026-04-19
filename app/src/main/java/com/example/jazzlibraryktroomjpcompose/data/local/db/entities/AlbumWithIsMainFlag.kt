package com.example.jazzlibraryktroomjpcompose.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class AlbumWithIsMainFlag(
    @Embedded val album: AlbumRoomEntity,
    @ColumnInfo(name = "is_main") val isMain: Int,

    @ColumnInfo(name = "artist_id") val artistId: Int?,

    @ColumnInfo(name = "artist_full_name") val artistFullName: String?,

    @ColumnInfo(name = "artist_instrument_id") val artistInstrumentId: Int?


)