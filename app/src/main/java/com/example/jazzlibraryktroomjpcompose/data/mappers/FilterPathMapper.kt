package com.example.jazzlibraryktroomjpcompose.data.mappers

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FilterPathMapper {

    fun toDisplayString(filterPath: FilterPath): String {
        val category = when (filterPath.categoryId) {
            FilterPath.CATEGORY_INSTRUMENT -> "Instrument"
            FilterPath.CATEGORY_ARTIST -> "Artist"
            FilterPath.CATEGORY_DURATION -> "Duration"
            FilterPath.CATEGORY_TYPE -> "Type"
            FilterPath.CATEGORY_SEARCH -> "Search"
            else -> "Unknown"
        }
        return if (filterPath.categoryId == FilterPath.CATEGORY_SEARCH) {
            val mode = when (filterPath.entityId) {
                0 -> "Video"
                1 -> "Artist"
                2 -> "Album"
                else -> "Unknown"
            }
            "$category ($mode): ${filterPath.entityName}"
        } else {
            "$category: ${filterPath.entityName} (ID: ${filterPath.entityId})"
        }
    }

    fun getCategoryName(categoryId: Int): String {
        return when (categoryId) {
            FilterPath.CATEGORY_INSTRUMENT -> "Instrument"
            FilterPath.CATEGORY_ARTIST -> "Artist"
            FilterPath.CATEGORY_DURATION -> "Duration"
            FilterPath.CATEGORY_TYPE -> "Type"
            FilterPath.CATEGORY_SEARCH -> "Search"
            else -> "Unknown"
        }
    }

    // Serialize filter list to a string
    fun serialize(filterPath: List<FilterPath>): String {
        val sorted = filterPath.sortedBy { it.categoryId }
        return sorted.joinToString("") { filter ->
            when (filter.categoryId) {
                FilterPath.CATEGORY_INSTRUMENT -> "I${filter.entityId}"
                FilterPath.CATEGORY_ARTIST -> "A${filter.entityId}"
                FilterPath.CATEGORY_DURATION -> "D${filter.entityId}"
                FilterPath.CATEGORY_TYPE -> "T${filter.entityId}"
                FilterPath.CATEGORY_SEARCH -> {
                    val encodedQuery = URLEncoder.encode(filter.entityName, StandardCharsets.UTF_8.name())
                    "Q${filter.entityId}:$encodedQuery"
                }
                else -> "?${filter.entityId}" // fallback for unknown categories
            }
        }
    }

    // Deserialize a string back to a list of FilterPath
    fun deserialize(serialNumber: String, timestamp: Long? = null): List<FilterPath> {
        // Match patterns:
        // - Non-search: [IADT]?(\d+)
        // - Search: Q(\d):([^:]*)  (the query may contain any chars, but we use URL encoding)
        val regex = Regex("([IADT]|Q\\d:)(\\d+|[^:]*)")
        return regex.findAll(serialNumber).mapNotNull { match ->
            val fullMatch = match.value
            when {
                fullMatch.startsWith("Q") -> {
                    // Format: Q{mode}:{encodedQuery}
                    val colonIndex = fullMatch.indexOf(':')
                    if (colonIndex == -1) return@mapNotNull null
                    val modeStr = fullMatch.substring(1, colonIndex)
                    val encodedQuery = fullMatch.substring(colonIndex + 1)
                    val mode = modeStr.toIntOrNull() ?: return@mapNotNull null
                    val query = try {
                        URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8.name())
                    } catch (e: Exception) {
                        ""
                    }
                    FilterPath(
                        autoIncrementId = 0,
                        categoryId = FilterPath.CATEGORY_SEARCH,
                        entityId = mode,
                        entityName = query
                    )
                }
                else -> {
                    // Non-search: single letter + digits
                    val categoryChar = fullMatch.take(1)
                    val idStr = fullMatch.drop(1)
                    val entityId = idStr.toIntOrNull() ?: return@mapNotNull null
                    val categoryId = when (categoryChar) {
                        "I" -> FilterPath.CATEGORY_INSTRUMENT
                        "A" -> FilterPath.CATEGORY_ARTIST
                        "D" -> FilterPath.CATEGORY_DURATION
                        "T" -> FilterPath.CATEGORY_TYPE
                        else -> return@mapNotNull null
                    }
                    FilterPath(
                        autoIncrementId = 0,
                        categoryId = categoryId,
                        entityId = entityId,
                        entityName = "" // Will be resolved later
                    )
                }
            }
        }.toList()
    }

    fun toEntity(serialNumber: String, timestamp: Long): FilterPathRoomEntity {
        return FilterPathRoomEntity(
            id = 0,
            serialNumber = serialNumber,
            timestamp = timestamp
        )
    }

    fun toDomain(entity: FilterPathRoomEntity): List<FilterPath> {
        return deserialize(entity.serialNumber, entity.timestamp)
    }
}