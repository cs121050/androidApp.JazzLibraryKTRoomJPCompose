package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Album

object AlbumMapper {
    fun toDomain(entity: AlbumRoomEntity): Album {
        return Album(
            albumId = entity.albumId,
            youtubeVideoIdForThumbnail = entity.youtubeVideoIdForThumbnail,
            ratingAverage = entity.ratingAverage,
            ratingCount = entity.ratingCount,
            released = entity.released,
            releaseType = entity.releaseType,
            title = entity.title,
            wikipediaUrl = entity.wikipediaUrl,
            coverartarchiveThumb = entity.coverartarchiveThumb,
            extraArtists = entity.extraArtists,
            genres = entity.genres,
            labels = entity.labels,
            styles = entity.styles,
            tracklist = entity.tracklist,
            wikipediaData = entity.wikipediaData
        )
    }

    fun toEntity(domain: Album): AlbumRoomEntity {
        return AlbumRoomEntity(
            albumId = domain.albumId,
            youtubeVideoIdForThumbnail = domain.youtubeVideoIdForThumbnail,
            ratingAverage = domain.ratingAverage,
            ratingCount = domain.ratingCount,
            released = domain.released,
            releaseType = domain.releaseType,
            title = domain.title,
            wikipediaUrl = domain.wikipediaUrl,
            coverartarchiveThumb = domain.coverartarchiveThumb,
            extraArtists = domain.extraArtists,
            genres = domain.genres,
            labels = domain.labels,
            styles = domain.styles,
            tracklist = domain.tracklist,
            wikipediaData = domain.wikipediaData
        )
    }
}