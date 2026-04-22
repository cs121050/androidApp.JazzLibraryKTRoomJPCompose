package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.FilterPathMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterPathRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPathRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : FilterPathRepository {

    // Insert a single filter as a new path entry (list of one)
    override suspend fun insertFilterPath(filterPath: FilterPath) {
        val serialized = FilterPathMapper.serialize(listOf(filterPath))
        val timestamp = System.currentTimeMillis()
        val entity = FilterPathRoomEntity(
            id = 0,
            serialNumber = serialized,
            timestamp = timestamp
        )
        database.filterPathDao().insertFilterPath(entity)   // ✅ now valid
    }

    // Get all individual filters from all stored paths (flattened)
    override fun getAllFilterPaths(): Flow<List<FilterPath>> =
        database.filterPathDao().getAllFilterPaths()
            .map { entities ->
                entities.flatMap { entity ->
                    FilterPathMapper.toDomain(entity)  // returns List<FilterPath>
                }
            }

    // Get the first filter of the latest path (or null if empty)
    override suspend fun getLatestFilterPath(): FilterPath? {
        val latestEntity = database.filterPathDao().getLatestFilterPath()
        return latestEntity?.let { entity ->
            FilterPathMapper.toDomain(entity).firstOrNull()
        }
    }

    override suspend fun deleteFilterPathsAfter(timestamp: Long) {
        database.filterPathDao().deleteFilterPathsAfter(timestamp)
    }

    // Get the first filter of the previous path relative to given timestamp
    override suspend fun getPreviousFilterPath(timestamp: Long): FilterPath? {
        val previousEntity = database.filterPathDao().getPreviousFilterPath(timestamp)
        return previousEntity?.let { entity ->
            FilterPathMapper.toDomain(entity).firstOrNull()
        }
    }

    override suspend fun deleteAllNewerThan(timestamp: Long) {
        database.filterPathDao().deleteAllNewerThan(timestamp)
    }

    // Insert a single filter as a new path and return its auto-generated ID
    override suspend fun insertFilterPathAndGetId(filterPath: FilterPath): Long {
        val serialized = FilterPathMapper.serialize(listOf(filterPath))
        val timestamp = System.currentTimeMillis()
        val entity = FilterPathRoomEntity(
            id = 0,
            serialNumber = serialized,
            timestamp = timestamp
        )
        return database.filterPathDao().insertFilterPathAndGetId(entity)
    }

    override suspend fun getAllHistoryEntries(): List<FilterHistoryEntry> {
        val daoEntries = database.filterPathDao().getAllHistoryEntries()
        return daoEntries.map { daoEntry ->
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
    }

    override suspend fun deleteAll() {
        database.filterPathDao().deleteAll()
    }

    override suspend fun getCount(): Int = database.filterPathDao().getCount()
}