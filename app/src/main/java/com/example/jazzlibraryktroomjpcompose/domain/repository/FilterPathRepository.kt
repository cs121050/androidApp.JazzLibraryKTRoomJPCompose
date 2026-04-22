package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.flow.Flow

data class FilterPathWithMeta(
    val id: Int,
    val filters: List<FilterPath>,
    val timestamp: Long
)

interface FilterPathRepository {
    // Insert a path (list of filters)
    suspend fun insertFilterPath(filterPath: List<FilterPath>)

    // Insert a path and return its auto-generated ID
    suspend fun insertFilterPathAndGetId(filterPath: List<FilterPath>): Long

    // Get all paths as a flow of flattened individual filters (if needed)
    fun getAllFilterPaths(): Flow<List<FilterPath>>

    // Get the latest path (full list of filters)
    suspend fun getLatestFilterPath(): List<FilterPath>?

    // Delete all paths newer than given timestamp
    suspend fun deleteAllNewerThan(timestamp: Long)

    // Get the previous path relative to timestamp
    suspend fun getPreviousFilterPath(timestamp: Long): List<FilterPath>?

    // Get all history entries (raw, with video associations)
    suspend fun getAllHistoryEntries(): List<FilterHistoryEntry>

    // Delete all paths
    suspend fun deleteAll()

    // Get count of stored paths
    suspend fun getCount(): Int

    // In FilterPathRepository interface
    suspend fun getLatestFilterPathId(): Int?
    suspend fun getLatestFilterPathTimestamp(): Long?

    suspend fun getLatestFilterPathWithMeta(): FilterPathWithMeta?

}