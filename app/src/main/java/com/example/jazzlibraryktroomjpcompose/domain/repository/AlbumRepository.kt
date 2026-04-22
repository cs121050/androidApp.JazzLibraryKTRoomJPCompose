package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumWithIsMainFlag
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    // Basic queries
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun insertAlbum(album: Album)
    suspend fun insertAllAlbums(albums: List<Album>)
    suspend fun updateAlbum(album: Album)
    suspend fun deleteAlbum(album: Album)
    suspend fun deleteAllAlbums()
    suspend fun getCount(): Int
    fun getAlbumById(id: Int): Flow<Album?>

    // Search
    fun searchAlbumsByTitle(query: String): Flow<List<Album>>

    // Custom queries
    fun getAlbumsByInstrument(instrumentId: Int): Flow<List<Album>>
    fun getAllAlbumsSortedByReleaseDateDesc(): Flow<List<Album>>
    fun getAllAlbumsSortedByReleaseDateAsc(): Flow<List<Album>>
    fun getAllAlbumsSortedByRatingDesc(): Flow<List<Album>>

    // In AlbumRepository interface
    fun getAlbumsByArtistAndInstrumentWithMainFlag(artistId: Int, instrumentId: Int): Flow<List<Album>>
    fun getAlbumByMultipleFilters(instrumentId: Int, artistId: Int): Flow<List<Album>>
}