package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumWithIsMainFlag
import com.example.jazzlibraryktroomjpcompose.data.mappers.AlbumMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPathContainsMediaRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        database.albumDao().getAllAlbums().map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    override suspend fun insertAlbum(album: Album) {
        database.albumDao().insertAlbum(AlbumMapper.toEntity(album))
    }

    override suspend fun insertAllAlbums(albums: List<Album>) {
        database.albumDao().insertAllAlbums(albums.map { AlbumMapper.toEntity(it) })
    }

    override suspend fun updateAlbum(album: Album) {
        database.albumDao().updateAlbum(AlbumMapper.toEntity(album))
    }

    override suspend fun deleteAlbum(album: Album) {
        database.albumDao().deleteAlbum(AlbumMapper.toEntity(album))
    }

    override suspend fun deleteAllAlbums() {
        database.albumDao().deleteAllAlbums()
    }

    override suspend fun getCount(): Int = database.albumDao().getCount()

    override fun getAlbumById(id: Int): Flow<Album?> =
        database.albumDao().getAlbumById(id).map { it?.let { AlbumMapper.toDomain(it) } }

    override fun searchAlbumsByTitle(query: String): Flow<List<Album>> =
        database.albumDao().searchAlbumsByTitle(query).map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    // In AlbumRepositoryImpl.kt
    override fun getAlbumsByArtistAndInstrumentWithMainFlag(artistId: Int, instrumentId: Int): Flow<List<Album>> =
        database.albumDao().getAlbumsByArtistAndInstrumentWithMainFlag(artistId, instrumentId)
            .map { entities -> entities.map { AlbumMapper.toDomainWithIsMainFlag(it) } }

    override fun getAlbumByMultipleFilters(instrumentId: Int, artistId: Int): Flow<List<Album>> =
        database.albumDao().getAlbumByMultipleFilters(instrumentId, artistId)
            .map { entities -> entities.map { AlbumMapper.toDomainWithIsMainFlag(it) } }

    override fun getAlbumsByInstrument(instrumentId: Int): Flow<List<Album>> =
        database.albumDao().getAlbumsByInstrument(instrumentId).map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    override fun getAllAlbumsSortedByReleaseDateDesc(): Flow<List<Album>> =
        database.albumDao().getAllAlbumsSortedByReleaseDateDesc().map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    override fun getAllAlbumsSortedByReleaseDateAsc(): Flow<List<Album>> =
        database.albumDao().getAllAlbumsSortedByReleaseDateAsc().map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    override fun getAllAlbumsSortedByRatingDesc(): Flow<List<Album>> =
        database.albumDao().getAllAlbumsSortedByRatingDesc().map { entities -> entities.map { AlbumMapper.toDomain(it) } }
}