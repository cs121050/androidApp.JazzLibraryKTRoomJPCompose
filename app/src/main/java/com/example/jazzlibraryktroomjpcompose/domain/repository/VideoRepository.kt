package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getAllVideos(): Flow<List<Video>>
    fun getVideoById(id: Int): Flow<Video?>
    suspend fun getVideoCount(): Int
}