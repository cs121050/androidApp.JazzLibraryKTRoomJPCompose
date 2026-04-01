package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

object FilterPathMapper {

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

    // Serialize filter list to a string
    fun serialize(filterPath: List<FilterPath>): String {
        val sorted = filterPath.sortedBy { it.categoryId }
        return sorted.joinToString("") { filter ->
            val categoryChar = when (filter.categoryId) {
                FilterPath.CATEGORY_INSTRUMENT -> "I"
                FilterPath.CATEGORY_ARTIST -> "A"
                FilterPath.CATEGORY_DURATION -> "D"
                FilterPath.CATEGORY_TYPE -> "T"
                // Add future categories:
                // FilterPath.CATEGORY_GENRE      -> "G"
                // FilterPath.CATEGORY_STYLE      -> "S"
                else -> "?"
            }
            "$categoryChar${filter.entityId}"
        }
    }

    // Deserialize a string back to a list of FilterPath
    fun deserialize(serialNumber: String, timestamp: Long? = null): List<FilterPath> {
        val regex = Regex("([IADTGSU]?)(\\d+)")
        return regex.findAll(serialNumber).map { match ->
            val (categoryChar, idStr) = match.destructured
            val categoryId = when (categoryChar) {
                "I" -> FilterPath.CATEGORY_INSTRUMENT
                "A" -> FilterPath.CATEGORY_ARTIST
                "D" -> FilterPath.CATEGORY_DURATION
                "T" -> FilterPath.CATEGORY_TYPE
                // Add future mappings:
                // "G" -> FilterPath.CATEGORY_GENRE
                // "S" -> FilterPath.CATEGORY_STYLE
                else -> 0
            }
            val entityId = idStr.toIntOrNull() ?: 0
            FilterPath(
                autoIncrementId = 0,
                categoryId = categoryId,
                entityId = entityId,
                entityName = ""     // Will be resolved later
            )
        }.toList()
    }


    // Local Entity → Domain
    fun toEntity(
        serialNumber: String,
        timestamp: Long
    ): FilterPathRoomEntity {
        return FilterPathRoomEntity(
            id = 0,
            serialNumber = serialNumber,
            timestamp = timestamp
        )
    }

    // Convert entity to domain list (names need to be filled later)
    fun toDomain(entity: FilterPathRoomEntity): List<FilterPath> {
        return deserialize(entity.serialNumber, entity.timestamp)
    }


}


