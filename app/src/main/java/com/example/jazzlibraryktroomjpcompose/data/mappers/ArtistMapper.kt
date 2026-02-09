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
            rank = domain.rank
        )
    }

    // Local Entity → Domain
    fun toDomain(entity: ArtistRoomEntity): Artist {
        return Artist(
            id = entity.id,
            name = entity.name,
            surname = entity.surname,
            instrumentId = entity.instrumentId,
            rank = entity.rank
        )
    }
    // Local Entity → Domain
    fun toDomainWithCount(entity: ArtistWithVideoCount): Artist {
        return Artist(
            id = entity.artist.id,
            name = entity.artist.name,
            surname = entity.artist.surname,
            instrumentId = entity.artist.instrumentId,
            rank = entity.artist.rank,
            videoCount = entity.videoCount
        )
    }

    //TODO//// Remote Response → Domain
    fun toDomain(response: ArtistResponse): Artist {
//        return Artist(
//            id = response.artistId,
//            name = response.artistName,
//            surname = response.artistSurname,
//            instrumentId = response.instrumentId ?: 0,
//            rank = response.artistRank
//        )
        return TODO("Provide the return value")
    }

    //TODO//// Domain → Remote Response (if needed for POST/PUT)
    fun toResponse(domain: Artist): ArtistResponse {
//        return ArtistResponse(
//            artistId = domain.id,
//            artistName = domain.name,
//            artistSurname = domain.surname,
//            instrumentId = domain.instrumentId,
//            artistRank = domain.rank
//        )
        return TODO("Provide the return value")
    }
}