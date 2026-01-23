package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.remote.models.QuoteResponse
import com.example.jazzlibraryktroomjpcompose.domain.models.Quote

object QuoteMapper {

    // Domain → Local Entity
    fun toEntity(domain: Quote): QuoteRoomEntity {
        return QuoteRoomEntity(
            id = domain.id,
            text = domain.text,
            artistId = domain.artistId,
            videoId = domain.videoId
        )
    }

    // Local Entity → Domain
    fun toDomain(entity: QuoteRoomEntity): Quote {
        return Quote(
            id = entity.id,
            text = entity.text,
            artistId = entity.artistId,
            videoId = entity.videoId
        )
    }

    //TODO//// Remote Response → Domain
    fun toDomain(response: QuoteResponse): Quote {
//        return Quote(
//            id = response.quoteId,
//            text = response.quoteName,
//            artistId = response.artistId ?: 0,

//        )
        return TODO("Provide the return value")
    }

    //TODO//// Domain → Remote Response (if needed for POST/PUT)
    fun toResponse(domain: Quote): QuoteResponse {
//        return QuoteResponse(
//            quoteId = domain.id,
//            quoteName = domain.name,
////          artistId = domain.artistId ?: 0,

//        )
        return TODO("Provide the return value")
    }
}