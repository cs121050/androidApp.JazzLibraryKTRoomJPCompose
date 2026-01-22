package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY artist_rank DESC, artist_name ASC")
    fun getAllArtists(): Flow<List<ArtistRoomEntity>>

    @Query("SELECT * FROM artists WHERE artist_id = :id")
    fun getArtistById(id: Int): Flow<ArtistRoomEntity>

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

    @Query("SELECT * FROM artists WHERE instrument_id = :instrumentId")
    fun getArtistsByInstrument(instrumentId: Int): Flow<List<ArtistRoomEntity>>

    @Query("SELECT * FROM artists WHERE artist_name LIKE '%' || :query || '%' OR artist_surname LIKE '%' || :query || '%'")
    fun searchArtists(query: String): Flow<List<ArtistRoomEntity>>
}