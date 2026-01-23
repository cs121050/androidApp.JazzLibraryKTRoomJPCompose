package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY video_name ASC")
    fun getAllVideos(): Flow<List<VideoRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVideos(videos: List<VideoRoomEntity>)

    @Update
    suspend fun updateVideo(video: VideoRoomEntity)

    @Delete
    suspend fun deleteVideo(video: VideoRoomEntity)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("SELECT * FROM videos WHERE video_id = :id")
    fun getVideoById(id: Int): Flow<VideoRoomEntity>

    @Query("SELECT * FROM videos WHERE video_name LIKE '%' || :query || '%'")
    fun searchVideosByName(query: String): Flow<List<VideoRoomEntity>>

    @Query("SELECT * FROM videos WHERE duration_id = :durationId")
    fun getVideosByDuration(durationId: Int): Flow<List<VideoRoomEntity>>

    @Query("SELECT * FROM videos WHERE type_id = :typeId")
    fun getVideosByType(typeId: Int): Flow<List<VideoRoomEntity>>

    @Query("SELECT * FROM videos WHERE location_id = :locationId")
    fun getVideosByLocation(locationId: String): Flow<List<VideoRoomEntity>>

    @Query("SELECT * FROM videos WHERE video_availability = :availability")
    fun getVideosByAvailability(availability: String): Flow<List<VideoRoomEntity>>
}