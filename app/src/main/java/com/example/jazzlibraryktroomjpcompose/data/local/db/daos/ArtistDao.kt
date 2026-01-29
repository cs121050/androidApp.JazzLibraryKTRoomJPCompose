package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY artist_rank DESC, artist_name ASC")
    fun getAllArtists(): Flow<List<ArtistRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArtists(artists: List<ArtistRoomEntity>)

    @Update
    suspend fun updateArtist(artist: ArtistRoomEntity)

    @Delete
    suspend fun deleteArtist(artist: ArtistRoomEntity)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()


    @Query("SELECT * FROM artists WHERE artist_id = :id")
    fun getArtistById(id: Int): Flow<ArtistRoomEntity>

    @Query("SELECT * FROM artists WHERE artist_name LIKE '%' || :query || '%' OR artist_surname LIKE '%' || :query || '%'")
    fun getArtistByName(query: String): Flow<List<ArtistRoomEntity>>

    @Query("SELECT * FROM artists WHERE instrument_id = :instrumentId")
    fun getArtistsByInstrument(instrumentId: Int): Flow<List<ArtistRoomEntity>>

    @Query("SELECT * FROM artists WHERE artist_rank = :rankId")
    fun getArtistsByRank(rankId :Int): Flow<List<ArtistRoomEntity>>




    // Filtering queries with composition
    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a 
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        WHERE a.instrument_id = :instrumentId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<ArtistWithVideoCount>>

    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        JOIN videos v ON v.video_id = video_contains_artist.video_id
        WHERE v.type_id = :typeId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByTypeWithVideoCount(typeId: Int): Flow<List<ArtistWithVideoCount>>

    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        JOIN videos v ON v.video_id = video_contains_artist.video_id
        WHERE v.duration_id = :durationId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByDurationWithVideoCount(durationId: Int): Flow<List<ArtistWithVideoCount>>


    // Combined filtering queries
    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        JOIN videos v ON v.video_id = video_contains_artist.video_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<ArtistWithVideoCount>>

    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        JOIN videos v ON v.video_id = video_contains_artist.video_id
        WHERE a.instrument_id = :instrumentId AND v.duration_id = :durationId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<ArtistWithVideoCount>>

    @Query("""
        SELECT a.*, COUNT(vca.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist vca ON vca.artist_id = a.artist_id
        JOIN videos v ON v.video_id = vca.video_id
        WHERE v.type_id = :typeId AND v.duration_id = :durationId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<ArtistWithVideoCount>>

    @Query("""
        SELECT a.*, COUNT(video_contains_artist.video_id) as video_count 
        FROM artists a
        JOIN video_contains_artist ON video_contains_artist.artist_id = a.artist_id
        JOIN videos v ON v.video_id = video_contains_artist.video_id
        WHERE a.instrument_id = :instrumentId AND v.type_id = :typeId AND v.duration_id = :durationId
        GROUP BY a.artist_id
        ORDER BY video_count DESC
    """)
    fun getArtistsByInstrumentAndTypeAndDurationWithVideoCount(
        instrumentId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<ArtistWithVideoCount>>

    @Query("""
    SELECT DISTINCT a.* FROM artists a
    INNER JOIN video_contains_artist vca ON a.artist_id = vca.artist_id
    INNER JOIN videos v ON vca.video_id = v.video_id
    WHERE (:instrumentId = 0 OR a.instrument_id = :instrumentId)
      AND (:typeId = 0 OR v.type_id = :typeId)
      AND (:durationId = 0 OR v.duration_id = :durationId)
    ORDER BY a.artist_name
""")
    fun getArtistsByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        durationId: Int = 0
    ): Flow<List<ArtistRoomEntity>>

}
