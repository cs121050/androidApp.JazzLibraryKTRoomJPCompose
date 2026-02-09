package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.remote.models.DurationResponse
import com.example.jazzlibraryktroomjpcompose.domain.models.Duration

object DurationMapper {

    fun toEntity(domain: Duration): DurationRoomEntity {
        return DurationRoomEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description
        )
    }

    fun toDomain(entity: DurationRoomEntity): Duration {
        return Duration(
            id = entity.id,
            name = entity.name,
            description = entity.description
        )
    }
    fun toDomainWithCount(entity: DurationWithVideoCount): Duration {
        return Duration(
            id = entity.duration.id,
            name = entity.duration.name,
            description = entity.duration.description,
            videoCount = entity.videoCount
        )
    }

    //TODO//// Remote Response → Domain
    fun toDomain(response: DurationResponse): Duration {
//        return Duration(
//            id = response.durationId,
//            name = response.durationName
//        )
        return TODO("Provide the return value")
    }

    //TODO//// Domain → Remote Response (if needed for POST/PUT)
    fun toResponse(domain: Duration): DurationResponse {
//        return DurationResponse(
//            durationId = domain.id,
//            durationName = domain.name
//        )
        return TODO("Provide the return value")
    }

}