package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoContainsArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist

object VideoContainsArtistMapper {
    fun toDomain(entity: VideoContainsArtistRoomEntity): VideoContainsArtist {
        return VideoContainsArtist(
            artistId = entity.artistId,
            videoId = entity.videoId
        )
    }

    fun toEntity(domain: VideoContainsArtist): VideoContainsArtistRoomEntity {
        return VideoContainsArtistRoomEntity(
            artistId = domain.artistId,
            videoId = domain.videoId
        )
    }
}