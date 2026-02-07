package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentWithVideoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instruments ORDER BY instrument_name ASC")
    fun getAllInstruments(): Flow<List<InstrumentRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: InstrumentRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInstruments(instruments: List<InstrumentRoomEntity>)

    @Delete
    suspend fun deleteInstrument(instrument: InstrumentRoomEntity)

    @Query("DELETE FROM instruments")
    suspend fun deleteAllInstruments()


    @Query("SELECT * FROM instruments WHERE instrument_id = :id")
    fun getInstrumentById(id: Int): Flow<InstrumentRoomEntity>

    @Query("SELECT * FROM instruments WHERE instrument_name LIKE '%' || :query || '%'")
    fun getInstrumentByName(query: String): Flow<List<InstrumentRoomEntity>>

    @Query("SELECT COUNT(*) FROM instruments")
    suspend fun getCount(): Int

    // Filtering queries with composition
    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        WHERE a.artist_id = :artistId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByArtistWithVideoCount(artistId: Int): Flow<List<InstrumentWithVideoCount>>

    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE v.type_id = :typeId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByTypeWithVideoCount(typeId: Int): Flow<List<InstrumentWithVideoCount>>

    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE v.duration_id = :durationId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByDurationWithVideoCount(durationId: Int): Flow<List<InstrumentWithVideoCount>>

    // Combined filtering queries - ALL COMBINATIONS
    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE a.artist_id = :artistId AND v.type_id = :typeId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByArtistAndTypeWithVideoCount(artistId: Int, typeId: Int): Flow<List<InstrumentWithVideoCount>>

    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE a.artist_id = :artistId AND v.duration_id = :durationId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<InstrumentWithVideoCount>>

    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE v.type_id = :typeId AND v.duration_id = :durationId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<InstrumentWithVideoCount>>

    @Query("""
        SELECT i.*, COUNT(vca.video_id) as video_count 
        FROM instruments i
        JOIN artists a ON a.instrument_id = i.instrument_id
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON vca.video_id = v.video_id
        WHERE a.artist_id = :artistId AND v.type_id = :typeId AND v.duration_id = :durationId
        GROUP BY i.instrument_id
        ORDER BY video_count DESC
    """)
    fun getInstrumentsByArtistAndTypeAndDurationWithVideoCount(
        artistId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<InstrumentWithVideoCount>>

    @Query("""
    SELECT DISTINCT i.* FROM instruments i
    INNER JOIN artists a ON i.instrument_id = a.instrument_id
    INNER JOIN video_contains_artist vca ON a.artist_id = vca.artist_id
    INNER JOIN videos v ON vca.video_id = v.video_id
    WHERE (:typeId = 0 OR v.type_id = :typeId)
      AND (:durationId = 0 OR v.duration_id = :durationId)
      AND (:artistId = 0 OR vca.artist_id = :artistId)
    ORDER BY i.instrument_name
""")
    fun getInstrumentsByMultipleFilters(
        typeId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<InstrumentRoomEntity>>


    @Query("SELECT COUNT(*) FROM instruments")
    suspend fun getInstrumentCount(): Int

}


