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


    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByArtist(artistId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrument(instrumentId: Int): Flow<List<VideoRoomEntity>>

    // Combined filtering queries - ALL COMBINATIONS (2 filters)
    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndType(instrumentId: Int, typeId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.duration_id = :durationId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndDuration(instrumentId: Int, durationId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndArtist(instrumentId: Int, artistId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId AND v.type_id = :typeId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByArtistAndType(artistId: Int, typeId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId AND v.duration_id = :durationId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByArtistAndDuration(artistId: Int, durationId: Int): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        WHERE v.type_id = :typeId AND v.duration_id = :durationId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByTypeAndDuration(typeId: Int, durationId: Int): Flow<List<VideoRoomEntity>>

    // Combined filtering queries - ALL COMBINATIONS (3 filters)
    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId AND v.duration_id = :durationId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndTypeAndDuration(
        instrumentId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndTypeAndArtist(
        instrumentId: Int,
        typeId: Int,
        artistId: Int
    ): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE v.type_id = :typeId AND v.duration_id = :durationId AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByTypeAndDurationAndArtist(
        typeId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<VideoRoomEntity>>

    // Combined filtering query - ALL 4 FILTERS
    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId 
          AND v.type_id = :typeId 
          AND v.duration_id = :durationId 
          AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByAllFilters(
        instrumentId: Int,
        typeId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId 
          AND v.duration_id = :durationId 
          AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByInstrumentAndDurationAndArtist(
        instrumentId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<VideoRoomEntity>>

    @Query("""
        SELECT v.* 
        FROM videos v 
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE v.type_id = :typeId 
          AND v.duration_id = :durationId 
          AND vca.artist_id = :artistId
        ORDER BY v.video_name ASC
    """)
    fun getVideosByArtistAndTypeAndDuration(
        artistId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<VideoRoomEntity>>

    @Query("""
    SELECT DISTINCT v.* FROM videos v
    INNER JOIN video_contains_artist vca ON v.video_id = vca.video_id
    INNER JOIN artists a ON vca.artist_id = a.artist_id
    WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
      AND (:typeId = 0 OR v.type_id = :typeId)
      AND (:durationId = 0 OR v.duration_id = :durationId)
      AND (:artistId = 0 OR vca.artist_id = :artistId)
    ORDER BY v.video_name
""")
    fun getVideosByMultipleFilters(
        instrumentId: Int = 0,
        artistId: Int = 0,
        durationId: Int = 0,
        typeId: Int = 0
    ): Flow<List<VideoRoomEntity>>




}