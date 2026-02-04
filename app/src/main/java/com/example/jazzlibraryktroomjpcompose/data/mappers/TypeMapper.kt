package com.example.jazzlibraryktroomjpcompose.data.mappers


import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Type
import com.example.jazzlibraryktroomjpcompose.domain.repository.TypeResponse

object TypeMapper {
    fun toEntity(domain: Type): TypeRoomEntity {
        return TypeRoomEntity(
            id = domain.id,
            name = domain.name
        )
    }

    fun toDomain(entity: TypeRoomEntity): Type {
        return Type(
            id = entity.id,
            name = entity.name
        )
    }

    //TODO//// Remote Response → Domain
    fun toDomain(response: TypeResponse): Type {
//        return Instrument(
//            id = response.instrumentId,
//            name = response.instrumentName
//        )
        return TODO("Provide the return value")
    }

    //TODO//// Domain → Remote Response (if needed for POST/PUT)
    fun toResponse(domain: Type): TypeResponse {
//        return InstrumentResponse(
//            instrumentId = domain.id,
//            instrumentName = domain.name
//        )
        return TODO("Provide the return value")
    }
}