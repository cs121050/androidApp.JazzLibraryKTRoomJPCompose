// data/repository/impl/AlbumRepositoryImpl.kt
package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.AlbumMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        database.albumDao().getAllAlbums()
            .map { entities -> entities.map { AlbumMapper.toDomain(it) } }

    override fun getAlbumById(id: Int): Flow<Album?> =
        database.albumDao().getAlbumById(id)
            .map { entity -> entity?.let { AlbumMapper.toDomain(it) } }
}