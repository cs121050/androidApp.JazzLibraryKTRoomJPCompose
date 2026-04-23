package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    suspend fun insertSong(song: Song)
    suspend fun insertAllSongs(songs: List<Song>)
    suspend fun deleteAllSongs()

    fun getSongsByAlbumId(albumId: Int): Flow<List<Song>>
}