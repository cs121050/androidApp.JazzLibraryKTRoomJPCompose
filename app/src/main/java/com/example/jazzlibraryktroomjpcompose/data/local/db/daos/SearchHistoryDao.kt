package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SearchHistoryRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SearchHistoryRoomEntity)


    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SearchHistoryRoomEntity>>

    @Query("SELECT * FROM search_history WHERE filter_path_id = :filterPathId")
    fun getByFilterPathId(filterPathId: Int): Flow<List<SearchHistoryRoomEntity>>

    // Get history entries filtered by is_media (nullable)
    @Query("SELECT * FROM search_history WHERE is_media = :isMedia ORDER BY timestamp DESC")
    fun getByIsMedia(isMedia: Int?): Flow<List<SearchHistoryRoomEntity>>

    // Get history entries with results_count greater than a threshold
    @Query("SELECT * FROM search_history WHERE results_count > :minResults ORDER BY timestamp DESC")
    fun getByResultsCountGreaterThan(minResults: Int): Flow<List<SearchHistoryRoomEntity>>

    // Get history entries for a specific filter_path_id and is_media value
    @Query("SELECT * FROM search_history WHERE filter_path_id = :filterPathId AND is_media = :isMedia")
    fun getByFilterPathIdAndIsMedia(filterPathId: Int, isMedia: Int?): Flow<List<SearchHistoryRoomEntity>>



    @Delete
    suspend fun delete(entry: SearchHistoryRoomEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun deleteAll()

}