package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.flow.Flow

interface FilterPathRepository {
    suspend fun insertFilterPath(filterPath: FilterPath)
    fun getAllFilterPaths(): Flow<List<FilterPath>>
    suspend fun getLatestFilterPath(): FilterPath?
    suspend fun deleteFilterPathsAfter(timestamp: Long)
    suspend fun getPreviousFilterPath(timestamp: Long): FilterPath?
    suspend fun deleteAllNewerThan(timestamp: Long)
    suspend fun insertFilterPathAndGetId(filterPath: FilterPath): Long
    suspend fun getAllHistoryEntries(): List<FilterHistoryEntry>
    suspend fun deleteAll()
    suspend fun getCount(): Int
}