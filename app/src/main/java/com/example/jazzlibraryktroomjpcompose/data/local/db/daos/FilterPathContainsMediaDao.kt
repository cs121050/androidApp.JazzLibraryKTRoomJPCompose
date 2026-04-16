package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathContainsMediaRoomEntity

@Dao
interface FilterPathContainsMediaDao {

    @Insert
    suspend fun insert(entry: FilterPathContainsMediaRoomEntity)

    @Query("SELECT video_id FROM filter_path_contains_media WHERE filter_path_room_entity_id = :filterPathId")
    suspend fun getVideoIdForFilterPath(filterPathId: Int): Int?

    @Query("DELETE FROM filter_path_contains_media WHERE filter_path_room_entity_id IN (SELECT id FROM filter_path WHERE timestamp > :timestamp)")
    suspend fun deleteAllNewerThan(timestamp: Long)

    @Query("DELETE FROM filter_path_contains_media")
    suspend fun deleteAll()
}