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

    // Album-Artist
    override suspend fun insertAlbumContainsArtist(albumArtist: AlbumContainsArtist) {
        database.albumContainsArtistDao().insertAlbumContainsArtist(AlbumContainsArtistMapper.toEntity(albumArtist))
    }

    override suspend fun insertAllAlbumContainsArtists(albumArtists: List<AlbumContainsArtist>) {
        database.albumContainsArtistDao().insertAllAlbumContainsArtists(albumArtists.map { AlbumContainsArtistMapper.toEntity(it) })
    }

    override suspend fun deleteAlbumContainsArtist(albumArtist: AlbumContainsArtist) {
        database.albumContainsArtistDao().deleteAlbumContainsArtist(AlbumContainsArtistMapper.toEntity(albumArtist))
    }

    override suspend fun deleteAllAlbumContainsArtists() {
        database.albumContainsArtistDao().deleteAllAlbumContainsArtists()
    }

    override fun getAllAlbumContainsArtists(): Flow<List<AlbumContainsArtist>> =
        database.albumContainsArtistDao().getAllAlbumContainsArtists()
            .map { entities -> entities.map { AlbumContainsArtistMapper.toDomain(it) } }

    override fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumContainsArtist>> =
        database.albumContainsArtistDao().getAlbumsByArtist(artistId)
            .map { entities -> entities.map { AlbumContainsArtistMapper.toDomain(it) } }

    override fun getArtistsByAlbum(albumId: Int): Flow<List<AlbumContainsArtist>> =
        database.albumContainsArtistDao().getArtistsByAlbum(albumId)
            .map { entities -> entities.map { AlbumContainsArtistMapper.toDomain(it) } }

    override suspend fun deleteSpecificAlbumArtist(artistId: Int, albumId: Int) {
        database.albumContainsArtistDao().deleteSpecificAlbumArtist(artistId, albumId)
    }

    override suspend fun deleteAllArtistsForAlbum(albumId: Int) {
        database.albumContainsArtistDao().deleteAllArtistsForAlbum(albumId)
    }

    override suspend fun deleteAllAlbumsForArtist(artistId: Int) {
        database.albumContainsArtistDao().deleteAllAlbumsForArtist(artistId)
    }

    override fun getAlbumIdsByArtist(artistId: Int): Flow<List<Int>> =
        database.albumContainsArtistDao().getAlbumIdsByArtist(artistId)

    // Video-Artist
    override suspend fun insertVideoContainsArtist(videoArtist: VideoContainsArtist) {
        database.videoContainsArtistDao().insertVideoContainsArtist(VideoContainsArtistMapper.toEntity(videoArtist))
    }

    override suspend fun insertAllVideoContainsArtists(videoArtists: List<VideoContainsArtist>) {
        database.videoContainsArtistDao().insertAllVideoContainsArtists(videoArtists.map { VideoContainsArtistMapper.toEntity(it) })
    }

    override suspend fun deleteVideoContainsArtist(videoArtist: VideoContainsArtist) {
        database.videoContainsArtistDao().deleteVideoContainsArtist(VideoContainsArtistMapper.toEntity(videoArtist))
    }

    override suspend fun deleteAllVideoContainsArtists() {
        database.videoContainsArtistDao().deleteAllVideoContainsArtists()
    }

    override fun getAllVideoContainsArtists(): Flow<List<VideoContainsArtist>> =
        database.videoContainsArtistDao().getAllVideoContainsArtists()
            .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }

    override fun getVideosByArtist(artistId: Int): Flow<List<VideoContainsArtist>> =
        database.videoContainsArtistDao().getVideosByArtist(artistId)
            .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }

    override fun getArtistsByVideo(videoId: Int): Flow<List<VideoContainsArtist>> =
        database.videoContainsArtistDao().getArtistsByVideo(videoId)
            .map { entities -> entities.map { VideoContainsArtistMapper.toDomain(it) } }

    override suspend fun deleteSpecificVideoArtist(artistId: Int, videoId: Int) {
        database.videoContainsArtistDao().deleteSpecificVideoArtist(artistId, videoId)
    }

    override suspend fun deleteAllArtistsForVideo(videoId: Int) {
        database.videoContainsArtistDao().deleteAllArtistsForVideo(videoId)
    }

    override suspend fun deleteAllVideosForArtist(artistId: Int) {
        database.videoContainsArtistDao().deleteAllVideosForArtist(artistId)
    }
}