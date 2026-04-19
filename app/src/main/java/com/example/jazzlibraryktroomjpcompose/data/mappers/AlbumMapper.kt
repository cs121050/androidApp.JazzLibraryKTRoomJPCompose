package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumWithIsMainFlag
import com.example.jazzlibraryktroomjpcompose.domain.models.Album

object AlbumMapper {
    fun toDomain(entity: AlbumRoomEntity): Album {
        return Album(
            albumId = entity.albumId,
            youtubeVideoIdForThumbnail = entity.youtubeVideoIdForThumbnail,
            ratingAverage = entity.ratingAverage,
            ratingCount = entity.ratingCount,
            year = entity.year,
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
            year = domain.year,
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

    fun toDomainWithIsMainFlag(entity: AlbumWithIsMainFlag): Album {
        return Album(
            albumId = entity.album.albumId,
            youtubeVideoIdForThumbnail = entity.album.youtubeVideoIdForThumbnail,
            ratingAverage = entity.album.ratingAverage,
            ratingCount = entity.album.ratingCount,
            year = entity.album.year,
            released = entity.album.released,
            releaseType = entity.album.releaseType,
            title = entity.album.title,
            wikipediaUrl = entity.album.wikipediaUrl,
            coverartarchiveThumb = entity.album.coverartarchiveThumb,
            extraArtists = entity.album.extraArtists,
            genres = entity.album.genres,
            labels = entity.album.labels,
            styles = entity.album.styles,
            tracklist = entity.album.tracklist,
            wikipediaData = entity.album.wikipediaData,
            isMain = entity.isMain,   // ← the flag from the JOIN
            artistId = entity.artistId,
            artistFullName = entity.artistFullName,
            artistInstrumentId = entity.artistInstrumentId
        )
    }
}