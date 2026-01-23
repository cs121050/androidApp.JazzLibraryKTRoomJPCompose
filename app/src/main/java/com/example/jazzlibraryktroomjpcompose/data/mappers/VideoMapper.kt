package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Video

object VideoMapper {
    fun toDomain(entity: VideoRoomEntity): Video {
        return Video(
            id = entity.id,
            name = entity.name,
            duration = entity.duration,
            path = entity.path,
            locationId = entity.locationId,
            availability = entity.availability,
            durationId = entity.durationId,
            typeId = entity.typeId
        )
    }

    fun toEntity(domain: Video): VideoRoomEntity {
        return VideoRoomEntity(
            id = domain.id,
            name = domain.name,
            duration = domain.duration,
            path = domain.path,
            locationId = domain.locationId,
            availability = domain.availability,
            durationId = domain.durationId,
            typeId = domain.typeId
        )
    }
}