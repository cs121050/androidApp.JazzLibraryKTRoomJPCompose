package com.example.jazzlibraryktroomjpcompose.data.local.db.daos


import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoContainsArtistRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoContainsArtistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoContainsArtist(videoArtist: VideoContainsArtistRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVideoContainsArtists(videoArtists: List<VideoContainsArtistRoomEntity>)

    @Delete
    suspend fun deleteVideoContainsArtist(videoArtist: VideoContainsArtistRoomEntity)

    @Query("DELETE FROM video_contains_artist")
    suspend fun deleteAllVideoContainsArtists()

    @Query("SELECT * FROM video_contains_artist")
    fun getAllVideoContainsArtists(): Flow<List<VideoContainsArtistRoomEntity>>


    @Query("SELECT * FROM video_contains_artist WHERE artist_id = :artistId")
    fun getVideosByArtist(artistId: Int): Flow<List<VideoContainsArtistRoomEntity>>

    @Query("SELECT * FROM video_contains_artist WHERE video_id = :videoId")
    fun getArtistsByVideo(videoId: Int): Flow<List<VideoContainsArtistRoomEntity>>

    @Query("DELETE FROM video_contains_artist WHERE artist_id = :artistId AND video_id = :videoId")
    suspend fun deleteSpecificVideoArtist(artistId: Int, videoId: Int)

    @Query("DELETE FROM video_contains_artist WHERE video_id = :videoId")
    suspend fun deleteAllArtistsForVideo(videoId: Int)

    @Query("DELETE FROM video_contains_artist WHERE artist_id = :artistId")
    suspend fun deleteAllVideosForArtist(artistId: Int)
}