package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument

object InstrumentMapper {

    // Domain → Local Entity
    fun toEntity(domain: Instrument): InstrumentRoomEntity {
        return InstrumentRoomEntity(
            id = domain.id,
            name = domain.name
        )
    }

    // Local Entity → Domain
    fun toDomain(entity: InstrumentRoomEntity): Instrument {
        return Instrument(
            id = entity.id,
            name = entity.name
        )
    }
    // Local Entity → Domain
    fun toDomainWithCount(entity: InstrumentWithVideoCount): Instrument {
        return Instrument(
            id = entity.instrument.id,
            name = entity.instrument.name,
            videoCount = entity.videoCount
        )
    }

}