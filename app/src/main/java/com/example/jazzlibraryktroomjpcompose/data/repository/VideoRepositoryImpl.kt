// data/repository/impl/VideoRepositoryImpl.kt
package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.VideoMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import com.example.jazzlibraryktroomjpcompose.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : VideoRepository {

    override fun getAllVideos(): Flow<List<Video>> =
        database.videoDao().getAllVideos()
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideoById(id: Int): Flow<Video?> =
        database.videoDao().getVideoById(id)
            .map { entity -> entity?.let { VideoMapper.toDomain(it) } }

    override suspend fun getVideoCount(): Int =
        database.videoDao().getCount()
}