package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    // Basic
    fun getAllVideos(): Flow<List<Video>>
    suspend fun insertVideo(video: Video)
    suspend fun insertAllVideos(videos: List<Video>)
    suspend fun updateVideo(video: Video)
    suspend fun deleteVideo(video: Video)
    suspend fun getCount(): Int
    suspend fun deleteAllVideos()

    // Single entity
    fun getVideoById(id: Int): Flow<Video?>

    // Search & filters – single criterion
    fun searchVideosByName(query: String): Flow<List<Video>>
    fun getVideosByDuration(durationId: Int): Flow<List<Video>>
    fun getVideosByType(typeId: Int): Flow<List<Video>>
    fun getVideosByLocation(locationId: String): Flow<List<Video>>
    fun getVideosByAvailability(availability: String): Flow<List<Video>>
    fun getVideosByArtist(artistId: Int): Flow<List<Video>>
    fun getVideosByInstrument(instrumentId: Int): Flow<List<Video>>

    // Two filters
    fun getVideosByInstrumentAndType(instrumentId: Int, typeId: Int): Flow<List<Video>>
    fun getVideosByInstrumentAndDuration(instrumentId: Int, durationId: Int): Flow<List<Video>>
    fun getVideosByInstrumentAndArtist(instrumentId: Int, artistId: Int): Flow<List<Video>>
    fun getVideosByArtistAndType(artistId: Int, typeId: Int): Flow<List<Video>>
    fun getVideosByArtistAndDuration(artistId: Int, durationId: Int): Flow<List<Video>>
    fun getVideosByTypeAndDuration(typeId: Int, durationId: Int): Flow<List<Video>>

    // Three filters
    fun getVideosByInstrumentAndTypeAndDuration(
        instrumentId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<Video>>

    fun getVideosByInstrumentAndTypeAndArtist(
        instrumentId: Int,
        typeId: Int,
        artistId: Int
    ): Flow<List<Video>>

    fun getVideosByTypeAndDurationAndArtist(
        typeId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<Video>>

    fun getVideosByInstrumentAndDurationAndArtist(
        instrumentId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<Video>>

    fun getVideosByArtistAndTypeAndDuration(
        artistId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<Video>>

    // Four filters
    fun getVideosByAllFilters(
        instrumentId: Int,
        typeId: Int,
        durationId: Int,
        artistId: Int
    ): Flow<List<Video>>

    // Flexible filters (all combinations with default 0 = ignore)
    fun getVideosByMultipleFilters(
        instrumentId: Int = 0,
        artistId: Int = 0,
        durationId: Int = 0,
        typeId: Int = 0
    ): Flow<List<Video>>
}