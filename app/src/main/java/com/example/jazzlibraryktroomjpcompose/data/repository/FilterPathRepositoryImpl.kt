// data/repository/impl/FilterPathRepositoryImpl.kt
package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterPathRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPathRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : FilterPathRepository {

    override fun getAllFilterPaths(): Flow<List<FilterPathRoomEntity>> =
        database.filterPathDao().getAllFilterPaths()   // Room Flow – fine

    override fun getLatestFilterPath(): Flow<FilterPathRoomEntity?> = flow {
        // DAO method is suspend, but flow builder provides coroutine scope
        emit(database.filterPathDao().getLatestFilterPath())
    }

    override suspend fun insertFilterPath(serial: String, timestamp: Long): Long =
        database.filterPathDao().insertFilterPathAndGetId(
            FilterPathRoomEntity(serialNumber = serial, timestamp = timestamp)
        )

    override suspend fun deleteAllNewerThan(timestamp: Long) {
        database.filterPathDao().deleteAllNewerThan(timestamp)
    }

    override suspend fun deleteAll() {
        database.filterPathDao().deleteAll()
    }

    override suspend fun getCount(): Int =
        database.filterPathDao().getCount()

    override fun getAllHistoryEntries(): Flow<List<FilterHistoryEntry>> = flow {
        // DAO returns a plain List (suspend), not a Flow
        val daoEntries = database.filterPathDao().getAllHistoryEntries()
        val domainEntries = daoEntries.map { daoEntry ->
            FilterHistoryEntry(
                filterPathId = daoEntry.filterPathId,
                serialNumber = daoEntry.serialNumber,
                timestamp = daoEntry.timestamp,
                videoId = daoEntry.videoId,
                videoName = daoEntry.videoName,
                videoPath = daoEntry.videoPath,
                locationId = daoEntry.locationId
            )
        }
        emit(domainEntries)
    }
}