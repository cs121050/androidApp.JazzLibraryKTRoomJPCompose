package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumContainsArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoContainsArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist

object AlbumContainsArtistMapper {
    fun toDomain(entity: AlbumContainsArtistRoomEntity): AlbumContainsArtist {
        return AlbumContainsArtist(
            artistId = entity.artistId,
            albumId = entity.albumId,
            isMain = entity.isMain
        )
    }

    fun toEntity(domain: AlbumContainsArtist): AlbumContainsArtistRoomEntity {
        return AlbumContainsArtistRoomEntity(
            artistId = domain.artistId,
            albumId = domain.albumId,
            isMain = domain.isMain
        )
    }
}