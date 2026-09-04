package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumContainsArtistRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumContainsArtistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbumContainsArtist(albumArtist: AlbumContainsArtistRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlbumContainsArtists(albumArtists: List<AlbumContainsArtistRoomEntity>)


    @Query("SELECT * FROM album_contains_artist")
    fun getAllAlbumContainsArtists(): Flow<List<AlbumContainsArtistRoomEntity>>

    @Query("SELECT * FROM album_contains_artist WHERE artist_id = :artistId")
    fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumContainsArtistRoomEntity>>

    @Query("SELECT album_id FROM album_contains_artist WHERE artist_id = :artistId")
    fun getAlbumIdsByArtist(artistId: Int): Flow<List<Int>>

    @Query("SELECT * FROM album_contains_artist WHERE album_id = :albumId")
    fun getArtistsByAlbum(albumId: Int): Flow<List<AlbumContainsArtistRoomEntity>>


    @Query("DELETE FROM album_contains_artist WHERE artist_id = :artistId AND album_id = :albumId")
    suspend fun deleteSpecificAlbumArtist(artistId: Int, albumId: Int)

    @Query("DELETE FROM album_contains_artist WHERE album_id = :albumId")
    suspend fun deleteAllArtistsForAlbum(albumId: Int)

    @Query("DELETE FROM album_contains_artist WHERE artist_id = :artistId")
    suspend fun deleteAllAlbumsForArtist(artistId: Int)

    @Delete
    suspend fun deleteAlbumContainsArtist(albumArtist: AlbumContainsArtistRoomEntity)

    @Query("DELETE FROM album_contains_artist")
    suspend fun deleteAllAlbumContainsArtists()

}