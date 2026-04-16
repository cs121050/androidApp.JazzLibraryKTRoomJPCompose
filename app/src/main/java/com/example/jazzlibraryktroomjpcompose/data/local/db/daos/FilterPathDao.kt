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

    // Add this method to FilterPathDao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterPathAndGetId(filterPath: FilterPathRoomEntity): Long

    data class HistoryEntry(
        val filterPathId: Int,
        val serialNumber: String,
        val timestamp: Long,
        val videoId: Int?,
        val videoName: String?,
        val videoPath: String?,
        val locationId: String?
    )

    @Query("""
    SELECT 
        f.id AS filterPathId,
        f.serial_number AS serialNumber,
        f.timestamp AS timestamp,
        v.video_id AS videoId,
        v.video_name AS videoName,
        v.video_path AS videoPath,
        v.location_id AS locationId
    FROM filter_path f
    LEFT JOIN filter_path_contains_media fcv ON fcv.filter_path_room_entity_id = f.id
    LEFT JOIN videos v ON v.video_id = fcv.video_id
    ORDER BY f.timestamp DESC
""")
    suspend  fun getAllHistoryEntries(): List<HistoryEntry>

    @Query("DELETE FROM filter_path")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM filter_path")
    suspend fun getCount(): Int
}