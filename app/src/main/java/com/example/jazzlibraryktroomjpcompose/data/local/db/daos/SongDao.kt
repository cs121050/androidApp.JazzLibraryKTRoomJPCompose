package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SongRoomEntity
import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSongs(songs: List<SongRoomEntity>)



    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongRoomEntity>>


    @Query("SELECT * FROM songs WHERE song_id = :songId")
    fun getSongById(songId: Int): Flow<SongRoomEntity>

    @Query("SELECT * FROM songs WHERE album_id = :albumId")
    fun getSongsByAlbumId(albumId: Int): Flow<List<SongRoomEntity>>



    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

}