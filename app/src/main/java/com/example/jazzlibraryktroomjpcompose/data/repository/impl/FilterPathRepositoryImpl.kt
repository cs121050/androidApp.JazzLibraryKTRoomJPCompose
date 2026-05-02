package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.FilterPathMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterHistoryEntry
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterPathRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterPathWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPathRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : FilterPathRepository {

    // Insert a path (list of filters)
    override suspend fun insertFilterPath(filterPath: List<FilterPath>) {
        val serialized = FilterPathMapper.serialize(filterPath)
        val entity = FilterPathRoomEntity(
            serialNumber = serialized,
            timestamp = System.currentTimeMillis()
        )
        database.filterPathDao().insertFilterPath(entity)
    }

    // Insert a path and return its ID
    override suspend fun insertFilterPathAndGetId(filterPath: List<FilterPath>): Long {
        val serialized = FilterPathMapper.serialize(filterPath)
        val entity = FilterPathRoomEntity(
            serialNumber = serialized,
            timestamp = System.currentTimeMillis()
        )
        return database.filterPathDao().insertFilterPathAndGetId(entity)
    }

    // Get the latest path (full list)
    override suspend fun getLatestFilterPath(): List<FilterPath>? {
        val entity = database.filterPathDao().getLatestFilterPath()
        return entity?.let { FilterPathMapper.toDomain(it) }
    }

    // Get previous path
    override suspend fun getPreviousFilterPath(timestamp: Long): List<FilterPath>? {
        val entity = database.filterPathDao().getPreviousFilterPath(timestamp)
        return entity?.let { FilterPathMapper.toDomain(it) }
    }

    // Get all individual filters from all stored paths (flattened)
    override fun getAllFilterPaths(): Flow<List<FilterPath>> =
        database.filterPathDao().getAllFilterPaths()
            .map { entities ->
                entities.flatMap { entity ->
                    FilterPathMapper.toDomain(entity)
                }
            }

    override suspend fun deleteAllNewerThan(timestamp: Long) {
        database.filterPathDao().deleteAllNewerThan(timestamp)
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
                locationId = daoEntry.locationId,
                typeOfMedia = daoEntry.typeOfMedia
            )
        }
    }

    override suspend fun deleteAll() {
        database.filterPathDao().deleteAll()
    }

    override suspend fun getCount(): Int = database.filterPathDao().getCount()

    override suspend fun getLatestFilterPathWithMeta(): FilterPathWithMeta? {
        val entity = database.filterPathDao().getLatestFilterPath() ?: return null
        return FilterPathWithMeta(
            id = entity.id,
            filters = FilterPathMapper.toDomain(entity),
            timestamp = entity.timestamp
        )
    }

    override suspend fun getLatestFilterPathId(): Int? {
        return database.filterPathDao().getLatestFilterPath()?.id
    }

    override suspend fun getLatestFilterPathTimestamp(): Long? {
        return database.filterPathDao().getLatestFilterPath()?.timestamp
    }



}