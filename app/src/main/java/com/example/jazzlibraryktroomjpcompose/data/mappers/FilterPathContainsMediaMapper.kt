// file: data/mappers/FilterPathContainsMediaMapper.kt
package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathContainsMediaRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPathContainsMedia

object FilterPathContainsMediaMapper {

    fun toDomain(entity: FilterPathContainsMediaRoomEntity): FilterPathContainsMedia =
        FilterPathContainsMedia(
            id = entity.id,
            filterPathId = entity.filterPathId,
            videoId = entity.videoId,
            typeOfMedia = entity.typeOfMedia
        )

    fun toEntity(domain: FilterPathContainsMedia): FilterPathContainsMediaRoomEntity =
        FilterPathContainsMediaRoomEntity(
            id = domain.id,
            filterPathId = domain.filterPathId,
            videoId = domain.videoId,
            typeOfMedia = domain.typeOfMedia
        )
}