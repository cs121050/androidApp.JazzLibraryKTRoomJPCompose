// data/repository/impl/AssociationRepositoryImpl.kt
package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.AlbumContainsArtistMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.VideoContainsArtistMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.repository.AssociationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssociationRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : AssociationRepository {

    override fun getAllVideoContainsArtists(): Flow<List<VideoContainsArtist>> =
        database.videoContainsArtistDao().getAllVideoContainsArtists()
            .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }

    override fun getAllAlbumContainsArtists(): Flow<List<AlbumContainsArtist>> =
        database.albumContainsArtistDao().getAllAlbumContainsArtists()
            .map { entities -> entities.map { AlbumContainsArtistMapper.toDomain(it) } }
}