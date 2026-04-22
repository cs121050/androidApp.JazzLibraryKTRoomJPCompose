// domain/repository/FilterPathRepository.kt
package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import kotlinx.coroutines.flow.Flow

interface FilterPathRepository {
    fun getAllFilterPaths(): Flow<List<FilterPathRoomEntity>>
    fun getLatestFilterPath(): Flow<FilterPathRoomEntity?>   // note: now returns Flow
    suspend fun insertFilterPath(serial: String, timestamp: Long): Long
    suspend fun deleteAllNewerThan(timestamp: Long)
    suspend fun deleteAll()
    suspend fun getCount(): Int
    fun getAllHistoryEntries(): Flow<List<FilterHistoryEntry>>   // ✅ domain type
}