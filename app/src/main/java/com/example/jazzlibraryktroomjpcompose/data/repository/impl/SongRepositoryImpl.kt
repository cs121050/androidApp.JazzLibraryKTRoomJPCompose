// file: data/repository/impl/SongRepositoryImpl.kt
package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.SongMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Song
import com.example.jazzlibraryktroomjpcompose.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> =
        database.songDao().getAllSongs()
            .map { entities -> entities.map { SongMapper.toDomain(it) } }

    override suspend fun insertSong(song: Song) {
        database.songDao().insertSong(SongMapper.toEntity(song))
    }

    override suspend fun insertAllSongs(songs: List<Song>) {
        database.songDao().insertAllSongs(songs.map { SongMapper.toEntity(it) })
    }

    override suspend fun deleteAllSongs() {
        database.songDao().deleteAllSongs()
    }

    override fun getSongById(songId: Int): Flow<Song?> =
        // Map the non‑nullable entity to nullable Song? to match the interface
        database.songDao().getSongById(songId).map { entity ->
            SongMapper.toDomain(entity) as Song?
        }

    override fun getSongsByAlbumId(albumId: Int): Flow<List<Song>> =
        database.songDao().getSongsByAlbumId(albumId)
            .map { entities -> entities.map { SongMapper.toDomain(it) } }
}