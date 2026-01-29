package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

object FilterPathMapper {

    // Local Entity → Domain
    fun toDomain(entity: FilterPathRoomEntity): FilterPath {
        return FilterPath(
            autoIncrementId = entity.autoIncrementId,
            categoryId = entity.categoryId,
            entityId = entity.entityId,
            entityName = entity.entityName
        )
    }

    // Domain → Local Entity
    fun toEntity(domain: FilterPath): FilterPathRoomEntity {
        return FilterPathRoomEntity(
            autoIncrementId = domain.autoIncrementId,
            categoryId = domain.categoryId,
            entityId = domain.entityId,
            entityName = domain.entityName
        )
    }

    // List conversion helpers
    fun toDomainList(entities: List<FilterPathRoomEntity>): List<FilterPath> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<FilterPath>): List<FilterPathRoomEntity> {
        return domains.map { toEntity(it) }
    }

    // Conversion with null safety
    fun toDomainOrNull(entity: FilterPathRoomEntity?): FilterPath? {
        return entity?.let { toDomain(it) }
    }

    fun toEntityOrNull(domain: FilterPath?): FilterPathRoomEntity? {
        return domain?.let { toEntity(it) }
    }

    // For debugging/display purposes
    fun toDisplayString(filterPath: FilterPath): String {
        val category = when (filterPath.categoryId) {
            1 -> "Instrument"
            2 -> "Artist"
            3 -> "Duration"
            4 -> "Type"
            else -> "Unknown"
        }
        return "$category: ${filterPath.entityName} (ID: ${filterPath.entityId})"
    }

    fun getCategoryName(categoryId: Int): String {
        return when (categoryId) {
            1 -> "Instrument"
            2 -> "Artist"
            3 -> "Duration"
            4 -> "Type"
            else -> "Unknown"
        }
    }
}