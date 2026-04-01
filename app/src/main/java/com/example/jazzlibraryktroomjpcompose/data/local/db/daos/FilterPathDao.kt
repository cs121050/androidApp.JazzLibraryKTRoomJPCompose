package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPathDao {

    @Insert
    suspend fun insertFilterPath(filterPath: FilterPathRoomEntity)

    @Query("""
    SELECT f.*, v.*
    FROM filter_path f
    LEFT JOIN videos v ON f.video_id = v.video_id
    ORDER BY f.timestamp DESC
    """)
    fun getAllFilterPaths(): Flow<List<FilterPathRoomEntity>>

    @Suppress("Unused")
    @Query(
    """
    WITH ranked AS (
        SELECT *,
               LAG(serial_number) OVER (ORDER BY timestamp DESC, auto_increment_id DESC) AS prev_serial
        FROM filter_path
    )
    SELECT auto_increment_id, serial_number, video_id, timestamp
    FROM ranked
    WHERE prev_serial IS NULL OR serial_number != prev_serial
    ORDER BY timestamp DESC, auto_increment_id DESC
    """
    )
    fun getAllFilterPathsWithoutConsecutiveDuplicates(): Flow<List<FilterPathRoomEntity>>

    @Query("SELECT * FROM filter_path WHERE timestamp < :currentTimestamp ORDER BY timestamp DESC LIMIT 1")
    suspend fun getPrevFilterPath(currentTimestamp: Long): FilterPathRoomEntity?

    @Query("SELECT * FROM filter_path ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFilterPath(): FilterPathRoomEntity?

    @Query("SELECT * FROM filter_path WHERE timestamp = :timestamp")
    suspend fun getFilterPathByTimestamp(timestamp: Long): FilterPathRoomEntity?

    @Query("DELETE FROM filter_path WHERE timestamp > :timestamp")
    suspend fun deleteAllNewerThan(timestamp: Long)

    @Query("DELETE FROM filter_path")
    suspend fun deleteAllFilterPaths()
}