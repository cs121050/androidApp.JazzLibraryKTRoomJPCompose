package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationWithVideoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface DurationDao {

    @Query("SELECT * FROM durations ORDER BY duration_name ASC")
    fun getAllDurations(): Flow<List<DurationRoomEntity>>

    // DurationDao.kt - Add if missing
    @Query("""
    SELECT d.*, COUNT(DISTINCT v.video_id) as video_count 
    FROM durations d
    LEFT JOIN videos v ON d.duration_id = v.duration_id
    GROUP BY d.duration_id, d.duration_name, d.duration_description
    ORDER BY d.duration_name ASC
""")
    fun getAllDurationsWithCount(): Flow<List<DurationWithVideoCount>>

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



    // Filtering queries with composition
    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        WHERE v.type_id = :typeId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByTypeWithVideoCount(typeId: Int): Flow<List<DurationWithVideoCount>>

    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByArtistWithVideoCount(artistId: Int): Flow<List<DurationWithVideoCount>>

    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<DurationWithVideoCount>>

    // Combined filtering queries - ALL COMBINATIONS
    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE v.type_id = :typeId AND vca.artist_id = :artistId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByTypeAndArtistWithVideoCount(typeId: Int, artistId: Int): Flow<List<DurationWithVideoCount>>

    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<DurationWithVideoCount>>

    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE vca.artist_id = :artistId AND a.instrument_id = :instrumentId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByArtistAndInstrumentWithVideoCount(artistId: Int, instrumentId: Int): Flow<List<DurationWithVideoCount>>

    @Query("""
        SELECT d.*, COUNT(v.video_id) as video_count 
        FROM durations d
        JOIN videos v ON v.duration_id = d.duration_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId AND vca.artist_id = :artistId
        GROUP BY d.duration_id
        ORDER BY video_count DESC
    """)
    fun getDurationsByInstrumentAndTypeAndArtistWithVideoCount(
        instrumentId: Int,
        typeId: Int,
        artistId: Int
    ): Flow<List<DurationWithVideoCount>>

    @Query("""
    SELECT DISTINCT d.* FROM durations d
    INNER JOIN videos v ON d.duration_id = v.duration_id
    INNER JOIN video_contains_artist vca ON v.video_id = vca.video_id
    INNER JOIN artists a ON vca.artist_id = a.artist_id
    WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
      AND (:typeId = 0 OR v.type_id = :typeId)
      AND (:artistId = 0 OR a.artist_id = :artistId)
    ORDER BY d.duration_name
""")
    fun getDurationsByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        artistId: Int = 0
    ): Flow<List<DurationRoomEntity>>

    @Query("""
    SELECT d.*, COUNT(DISTINCT v.video_id) as video_count 
FROM durations d
INNER JOIN videos v ON d.duration_id = v.duration_id
INNER JOIN video_contains_artist vca ON v.video_id = vca.video_id
INNER JOIN artists a ON vca.artist_id = a.artist_id
WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
  AND (:typeId = 0 OR v.type_id = :typeId)
  AND (:artistId = 0 OR a.artist_id = :artistId)
GROUP BY d.duration_id, d.duration_name, d.duration_description
""")
    fun getDurationsWithVideoCountByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        artistId: Int = 0
    ): Flow<List<DurationWithVideoCount>>

}