package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.remote.models.ArtistResponse
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist

// Mapper ensures:
// 1. Each class belongs to ONE architecture layer
// 2. Each class contains ONLY what that layer needs
// 3. No layer knows about other layers' implementation details
//it's the converter
object ArtistMapper {

    // Domain → Local Entity
    fun toEntity(domain: Artist): ArtistRoomEntity {
        return ArtistRoomEntity(
            id = domain.id,
            name = domain.name,
            surname = domain.surname,
            instrumentId = domain.instrumentId,
            rank = domain.rank,
            spotifyPlaylistId = domain.spotifyPlaylistId,
            musicbrainzUUID = domain.musicbrainzUUID,
            discogsId = domain.discogsId,
            wikipediaUrl = domain.wikipediaUrl,
            thumbnailUrl = domain.thumbnailUrl,
            imageAuthor = domain.imageAuthor,
            imageLicense = domain.imageLicense,
            imageSourceUrl = domain.imageSourceUrl,
            wikipediaData = domain.wikipediaData
        )
    }

    // Local Entity → Domain
    fun toDomain(entity: ArtistRoomEntity): Artist {
        return Artist(
            id = entity.id,
            name = entity.name,
            surname = entity.surname,
            instrumentId = entity.instrumentId,
            rank = entity.rank,
            spotifyPlaylistId = entity.spotifyPlaylistId,
            musicbrainzUUID = entity.musicbrainzUUID,
            discogsId = entity.discogsId,
            wikipediaUrl = entity.wikipediaUrl,
            thumbnailUrl = entity.thumbnailUrl,
            imageAuthor = entity.imageAuthor,
            imageLicense = entity.imageLicense,
            imageSourceUrl = entity.imageSourceUrl,
            wikipediaData = entity.wikipediaData
        )
    }

    // Local Entity (with video count) → Domain
    fun toDomainWithCount(entity: ArtistWithVideoCount): Artist {
        return Artist(
            id = entity.artist.id,
            name = entity.artist.name,
            surname = entity.artist.surname,
            instrumentId = entity.artist.instrumentId,
            rank = entity.artist.rank,
            spotifyPlaylistId = entity.artist.spotifyPlaylistId,
            musicbrainzUUID = entity.artist.musicbrainzUUID,
            discogsId = entity.artist.discogsId,
            wikipediaUrl = entity.artist.wikipediaUrl,
            thumbnailUrl = entity.artist.thumbnailUrl,
            imageAuthor = entity.artist.imageAuthor,
            imageLicense = entity.artist.imageLicense,
            imageSourceUrl = entity.artist.imageSourceUrl,
            wikipediaData = entity.artist.wikipediaData,
            videoCount = entity.videoCount
        )
    }

    // Remote Response → Domain (placeholder)
    fun toDomain(response: ArtistResponse): Artist {
        // TODO: Implement when remote model is updated
        return TODO("Provide the return value")
    }

    // Domain → Remote Response (placeholder)
    fun toResponse(domain: Artist): ArtistResponse {
        // TODO: Implement when remote model is updated
        return TODO("Provide the return value")
    }
}