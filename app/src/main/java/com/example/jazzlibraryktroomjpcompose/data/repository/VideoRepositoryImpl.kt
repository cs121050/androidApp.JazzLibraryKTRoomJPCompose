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
        database.videoDao().getAllVideos().map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override suspend fun insertVideo(video: Video) {
        database.videoDao().insertVideo(VideoMapper.toEntity(video))
    }

    override suspend fun insertAllVideos(videos: List<Video>) {
        database.videoDao().insertAllVideos(videos.map { VideoMapper.toEntity(it) })
    }

    override suspend fun updateVideo(video: Video) {
        database.videoDao().updateVideo(VideoMapper.toEntity(video))
    }

    override suspend fun deleteVideo(video: Video) {
        database.videoDao().deleteVideo(VideoMapper.toEntity(video))
    }

    override suspend fun getCount(): Int = database.videoDao().getCount()

    override suspend fun deleteAllVideos() {
        database.videoDao().deleteAllVideos()
    }

    override fun getVideoById(id: Int): Flow<Video?> =
        database.videoDao().getVideoById(id).map { it?.let { VideoMapper.toDomain(it) } }

    override fun searchVideosByName(query: String): Flow<List<Video>> =
        database.videoDao().searchVideosByName(query).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByDuration(durationId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByDuration(durationId).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByType(typeId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByType(typeId).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByLocation(locationId: String): Flow<List<Video>> =
        database.videoDao().getVideosByLocation(locationId).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByAvailability(availability: String): Flow<List<Video>> =
        database.videoDao().getVideosByAvailability(availability).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByArtist(artistId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByArtist(artistId).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByInstrument(instrumentId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByInstrument(instrumentId).map { entities -> entities.map { VideoMapper.toDomain(it) } }

    // Two filters
    override fun getVideosByInstrumentAndType(instrumentId: Int, typeId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndType(instrumentId, typeId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByInstrumentAndDuration(instrumentId: Int, durationId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndDuration(instrumentId, durationId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByInstrumentAndArtist(instrumentId: Int, artistId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndArtist(instrumentId, artistId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByArtistAndType(artistId: Int, typeId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByArtistAndType(artistId, typeId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByArtistAndDuration(artistId: Int, durationId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByArtistAndDuration(artistId, durationId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByTypeAndDuration(typeId: Int, durationId: Int): Flow<List<Video>> =
        database.videoDao().getVideosByTypeAndDuration(typeId, durationId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    // Three filters
    override fun getVideosByInstrumentAndTypeAndDuration(
        instrumentId: Int, typeId: Int, durationId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndTypeAndDuration(instrumentId, typeId, durationId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByInstrumentAndTypeAndArtist(
        instrumentId: Int, typeId: Int, artistId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndTypeAndArtist(instrumentId, typeId, artistId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByTypeAndDurationAndArtist(
        typeId: Int, durationId: Int, artistId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByTypeAndDurationAndArtist(typeId, durationId, artistId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByInstrumentAndDurationAndArtist(
        instrumentId: Int, durationId: Int, artistId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByInstrumentAndDurationAndArtist(instrumentId, durationId, artistId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    override fun getVideosByArtistAndTypeAndDuration(
        artistId: Int, typeId: Int, durationId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByArtistAndTypeAndDuration(artistId, typeId, durationId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    // Four filters
    override fun getVideosByAllFilters(
        instrumentId: Int, typeId: Int, durationId: Int, artistId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByAllFilters(instrumentId, typeId, durationId, artistId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }

    // Flexible filters
    override fun getVideosByMultipleFilters(
        instrumentId: Int, artistId: Int, durationId: Int, typeId: Int
    ): Flow<List<Video>> =
        database.videoDao().getVideosByMultipleFilters(instrumentId, artistId, durationId, typeId)
            .map { entities -> entities.map { VideoMapper.toDomain(it) } }
}