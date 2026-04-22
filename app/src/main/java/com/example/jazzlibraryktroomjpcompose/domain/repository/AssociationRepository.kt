package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist
import kotlinx.coroutines.flow.Flow

interface AssociationRepository {
    // Album-Artist
    suspend fun insertAlbumContainsArtist(albumArtist: AlbumContainsArtist)
    suspend fun insertAllAlbumContainsArtists(albumArtists: List<AlbumContainsArtist>)
    suspend fun deleteAlbumContainsArtist(albumArtist: AlbumContainsArtist)
    suspend fun deleteAllAlbumContainsArtists()
    fun getAllAlbumContainsArtists(): Flow<List<AlbumContainsArtist>>
    fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumContainsArtist>>
    fun getArtistsByAlbum(albumId: Int): Flow<List<AlbumContainsArtist>>
    suspend fun deleteSpecificAlbumArtist(artistId: Int, albumId: Int)
    suspend fun deleteAllArtistsForAlbum(albumId: Int)
    suspend fun deleteAllAlbumsForArtist(artistId: Int)
    fun getAlbumIdsByArtist(artistId: Int): Flow<List<Int>>

    // Video-Artist
    suspend fun insertVideoContainsArtist(videoArtist: VideoContainsArtist)
    suspend fun insertAllVideoContainsArtists(videoArtists: List<VideoContainsArtist>)
    suspend fun deleteVideoContainsArtist(videoArtist: VideoContainsArtist)
    suspend fun deleteAllVideoContainsArtists()
    fun getAllVideoContainsArtists(): Flow<List<VideoContainsArtist>>
    fun getVideosByArtist(artistId: Int): Flow<List<VideoContainsArtist>>
    fun getArtistsByVideo(videoId: Int): Flow<List<VideoContainsArtist>>
    suspend fun deleteSpecificVideoArtist(artistId: Int, videoId: Int)
    suspend fun deleteAllArtistsForVideo(videoId: Int)
    suspend fun deleteAllVideosForArtist(artistId: Int)
}