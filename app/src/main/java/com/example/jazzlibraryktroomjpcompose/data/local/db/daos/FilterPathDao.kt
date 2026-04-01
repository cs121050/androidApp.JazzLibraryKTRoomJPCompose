package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPathDao {
    @Insert
    suspend fun insertFilterPath(filterPath: FilterPathRoomEntity)

    @Query("SELECT * FROM filter_path ORDER BY timestamp DESC")
    fun getAllFilterPaths(): Flow<List<FilterPathRoomEntity>>

    @Query("SELECT * FROM filter_path ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFilterPath(): FilterPathRoomEntity?

    @Query("DELETE FROM filter_path WHERE timestamp > :timestamp")
    suspend fun deleteFilterPathsAfter(timestamp: Long)

    @Query("SELECT * FROM filter_path WHERE timestamp < :timestamp ORDER BY timestamp DESC LIMIT 1")
    suspend fun getPreviousFilterPath(timestamp: Long): FilterPathRoomEntity?

    @Query("DELETE FROM filter_path WHERE timestamp > :timestamp")
    suspend fun deleteAllNewerThan(timestamp: Long)
}