package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DurationDao {

    @Query("SELECT * FROM durations ORDER BY duration_name ASC")
    fun getAllDurations(): Flow<List<DurationRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuration(duration: DurationRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDurations(durations: List<DurationRoomEntity>)

    @Update
    suspend fun updateDuration(duration: DurationRoomEntity)

    @Delete
    suspend fun deleteDuration(duration: DurationRoomEntity)

    @Query("DELETE FROM durations")
    suspend fun deleteAllDurations()

    @Query("SELECT * FROM durations WHERE duration_id = :id")
    fun getDurationById(id: Int): Flow<DurationRoomEntity>

    @Query("SELECT * FROM durations WHERE duration_name LIKE '%' || :query || '%' OR duration_description LIKE '%' || :query || '%'")
    fun searchDurations(query: String): Flow<List<DurationRoomEntity>>
}