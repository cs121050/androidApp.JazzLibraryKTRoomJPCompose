package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumWithIsMainFlag
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    // Basic queries
    @Query("SELECT * FROM albums ORDER BY released DESC")
    fun getAllAlbums(): Flow<List<AlbumRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlbums(albums: List<AlbumRoomEntity>)

    @Update
    suspend fun updateAlbum(album: AlbumRoomEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumRoomEntity)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getCount(): Int

    @Query("SELECT * FROM albums WHERE album_id = :id")
    fun getAlbumById(id: Int): Flow<AlbumRoomEntity>

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchAlbumsByTitle(query: String): Flow<List<AlbumRoomEntity>>

    @Query("""
    SELECT DISTINCT a.*, aca.is_main, art.artist_id, art.artist_name || ' ' || art.artist_surname AS artist_full_name, art.instrument_id AS artist_instrument_id
    FROM albums a
    JOIN album_contains_artist aca ON aca.album_id = a.album_id
    JOIN artists art ON art.artist_id = aca.artist_id
    WHERE aca.artist_id = :artistId
      AND (:instrumentId = 0 OR art.instrument_id = :instrumentId)
      AND (a.title LIKE '%' || :searchQuery || '%')
    ORDER BY a.title DESC
""")
    fun getAlbumsByArtistAndInstrumentWithMainFlag(
        artistId: Int,
        instrumentId: Int,
        searchQuery: String = ""
        ): Flow<List<AlbumWithIsMainFlag>>

    // Single filter – by instrument
    @Query("""
        SELECT a.* 
        FROM albums a 
        JOIN album_contains_artist aca ON aca.album_id = a.album_id
        JOIN artists art ON art.artist_id = aca.artist_id
        WHERE art.instrument_id = :instrumentId
        ORDER BY a.released DESC
    """)
    fun getAlbumsByInstrument(instrumentId: Int): Flow<List<AlbumRoomEntity>>

    // Double filter – artist AND instrument
    @Query("""
    SELECT DISTINCT a.*, aca.is_main, art.artist_id, art.artist_name || ' ' || art.artist_surname AS artist_full_name, art.instrument_id AS artist_instrument_id
    FROM albums a 
    LEFT JOIN album_contains_artist aca ON aca.album_id = a.album_id
    LEFT JOIN artists art ON art.artist_id = aca.artist_id
    WHERE (:instrumentId = 0 OR art.instrument_id = :instrumentId)
      AND (:artistId = 0 OR aca.artist_id = :artistId)
      AND aca.is_main = 1
      AND (a.title LIKE '%' || :searchQuery || '%')
    ORDER BY a.title DESC
""")
    fun getAlbumByMultipleFilters(
        instrumentId: Int,
        artistId: Int,
        searchQuery: String = ""
    ): Flow<List<AlbumWithIsMainFlag>>

    @Query("SELECT * FROM albums ORDER BY released DESC")
    fun getAllAlbumsSortedByReleaseDateDesc(): Flow<List<AlbumRoomEntity>>

    @Query("SELECT * FROM albums ORDER BY released ASC")
    fun getAllAlbumsSortedByReleaseDateAsc(): Flow<List<AlbumRoomEntity>>

    @Query("SELECT * FROM albums ORDER BY rating_average IS NULL, rating_average DESC")
    fun getAllAlbumsSortedByRatingDesc(): Flow<List<AlbumRoomEntity>>
}