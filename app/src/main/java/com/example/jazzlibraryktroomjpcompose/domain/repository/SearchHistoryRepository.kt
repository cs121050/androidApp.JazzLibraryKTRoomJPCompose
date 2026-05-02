package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getAllSearchHistory(): Flow<List<SearchHistoryRoomEntity>>
    suspend fun insertSearchHistory(entry: SearchHistoryRoomEntity)
    suspend fun deleteSearchHistory(entry: SearchHistoryRoomEntity)
    suspend fun deleteSearchHistoryById(id: Long)
    suspend fun deleteAllSearchHistory()
    fun getByFilterPathId(filterPathId: Int): Flow<List<SearchHistoryRoomEntity>>
}