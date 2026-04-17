package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SongRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Song

object SongMapper {
    fun toDomain(entity: SongRoomEntity): Song {
        return Song(
            songId = entity.songId,
            mainArtistId = entity.mainArtistId,
            relatedArtists = entity.relatedArtists,
            albumId = entity.albumId,
            songTitle = entity.songTitle,
            duration = entity.duration,
            ytVideoId = entity.ytVideoId,
            videoAvailability = entity.videoAvailability
        )
    }

    fun toEntity(domain: Song): SongRoomEntity {
        return SongRoomEntity(
            songId = domain.songId,
            mainArtistId = domain.mainArtistId,
            relatedArtists = domain.relatedArtists,
            albumId = domain.albumId,
            songTitle = domain.songTitle,
            duration = domain.duration,
            ytVideoId = domain.ytVideoId,
            videoAvailability = domain.videoAvailability
        )
    }
}