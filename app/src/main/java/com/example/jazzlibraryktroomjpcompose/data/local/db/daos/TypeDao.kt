package com.example.jazzlibraryktroomjpcompose.data.local.db.daos


import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeWithVideoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {

    @Query("SELECT * FROM types ORDER BY type_name ASC")
    fun getAllTypes(): Flow<List<TypeRoomEntity>>

    @Query("""
    SELECT t.*, COUNT(DISTINCT v.video_id) as video_count 
    FROM types t
    LEFT JOIN videos v ON t.type_id = v.type_id
    GROUP BY t.type_id, t.type_name
    ORDER BY t.type_name ASC
""")
    fun getAllTypesWithCount(): Flow<List<TypeWithVideoCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: TypeRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTypes(types: List<TypeRoomEntity>)

    @Update
    suspend fun updateType(type: TypeRoomEntity)

    @Delete
    suspend fun deleteType(type: TypeRoomEntity)

    @Query("DELETE FROM types")
    suspend fun deleteAllTypes()

    @Query("SELECT * FROM types WHERE type_id = :id")
    fun getTypeById(id: Int): Flow<TypeRoomEntity>

    @Query("SELECT * FROM types WHERE type_name LIKE '%' || :query || '%'")
    fun searchTypes(query: String): Flow<List<TypeRoomEntity>>


    // Filtering queries with composition
    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        WHERE v.duration_id = :durationId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByDurationWithVideoCount(durationId: Int): Flow<List<TypeWithVideoCount>>

    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByArtistWithVideoCount(artistId: Int): Flow<List<TypeWithVideoCount>>

    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByInstrumentWithVideoCount(instrumentId: Int): Flow<List<TypeWithVideoCount>>


    // Combined filtering queries - ALL COMBINATIONS
    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        WHERE vca.artist_id = :artistId AND v.duration_id = :durationId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<TypeWithVideoCount>>

    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND v.duration_id = :durationId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<TypeWithVideoCount>>

    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND vca.artist_id = :artistId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByInstrumentAndArtistWithVideoCount(instrumentId: Int, artistId: Int): Flow<List<TypeWithVideoCount>>

    @Query("""
        SELECT t.*, COUNT(v.video_id) as video_count 
        FROM types t 
        JOIN videos v ON v.type_id = t.type_id
        JOIN video_contains_artist vca ON vca.video_id = v.video_id
        JOIN artists a ON a.artist_id = vca.artist_id
        WHERE a.instrument_id = :instrumentId AND vca.artist_id = :artistId AND v.duration_id = :durationId
        GROUP BY t.type_id
        ORDER BY video_count DESC
    """)
    fun getTypesByInstrumentAndArtistAndDurationWithVideoCount(
        instrumentId: Int,
        artistId: Int,
        durationId: Int
    ): Flow<List<TypeWithVideoCount>>

    @Query("""
    SELECT DISTINCT t.* FROM types t
    INNER JOIN videos v ON t.type_id = v.type_id
    INNER JOIN video_contains_artist vca ON v.video_id = vca.video_id
    INNER JOIN artists a ON vca.artist_id = a.artist_id
    WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
      AND (:durationId = 0 OR v.duration_id = :durationId)
      AND (:artistId = 0 OR a.artist_id = :artistId)
    ORDER BY t.type_name
""")
    fun getTypesByMultipleFilters(
        instrumentId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<TypeRoomEntity>>

    @Query("""
    SELECT t.*, COUNT(DISTINCT v.video_id) as video_count 
        FROM types t
        INNER JOIN videos v ON t.type_id = v.type_id
        INNER JOIN video_contains_artist vca ON v.video_id = vca.video_id
        INNER JOIN artists a ON vca.artist_id = a.artist_id
        WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
          AND (:durationId = 0 OR v.duration_id = :durationId)
          AND (:artistId = 0 OR a.artist_id = :artistId)
        GROUP BY t.type_id, t.type_name
""")
    fun getTypesWithVideoCountByMultipleFilters(
        instrumentId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<TypeWithVideoCount>>

}