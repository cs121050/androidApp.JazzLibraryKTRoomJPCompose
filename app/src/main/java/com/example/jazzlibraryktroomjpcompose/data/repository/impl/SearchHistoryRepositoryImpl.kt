package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : SearchHistoryRepository {

    override fun getAllSearchHistory(): Flow<List<SearchHistoryRoomEntity>> =
        database.searchHistoryDao().getAll()

    override suspend fun insertSearchHistory(entry: SearchHistoryRoomEntity) =
        database.searchHistoryDao().insert(entry)

    override suspend fun deleteSearchHistory(entry: SearchHistoryRoomEntity) =
        database.searchHistoryDao().delete(entry)

    override suspend fun deleteSearchHistoryById(id: Long) =
        database.searchHistoryDao().deleteById(id)

    override suspend fun deleteAllSearchHistory() =
        database.searchHistoryDao().deleteAll()

    override fun getByFilterPathId(filterPathId: Int): Flow<List<SearchHistoryRoomEntity>> =
        database.searchHistoryDao().getByFilterPathId(filterPathId)
    
    
}