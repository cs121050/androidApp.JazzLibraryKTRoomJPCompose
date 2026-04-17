package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
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

    // Single filter – by artist
    @Query("""
        SELECT a.* 
        FROM albums a 
        JOIN album_contains_artist aca ON aca.album_id = a.album_id
        WHERE aca.artist_id = :artistId
        ORDER BY a.released DESC
    """)
    fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumRoomEntity>>

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
    SELECT DISTINCT a.* 
    FROM albums a 
    LEFT JOIN album_contains_artist aca ON aca.album_id = a.album_id
    LEFT JOIN artists art ON art.artist_id = aca.artist_id
    WHERE (:instrumentId = 0 OR art.instrument_id = :instrumentId)
      AND (:artistId = 0 OR aca.artist_id = :artistId)
    ORDER BY a.released DESC
""")
    fun getAlbumByMultipleFilters(instrumentId: Int, artistId: Int): Flow<List<AlbumRoomEntity>>

    @Query("SELECT * FROM albums ORDER BY released DESC")
    fun getAllAlbumsSortedByReleaseDateDesc(): Flow<List<AlbumRoomEntity>>

    @Query("SELECT * FROM albums ORDER BY released ASC")
    fun getAllAlbumsSortedByReleaseDateAsc(): Flow<List<AlbumRoomEntity>>

    @Query("SELECT * FROM albums ORDER BY rating_average IS NULL, rating_average DESC")
    fun getAllAlbumsSortedByRatingDesc(): Flow<List<AlbumRoomEntity>>
}