package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.AlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.VideoContainsArtist
import kotlinx.coroutines.flow.Flow

interface AssociationRepository {
    fun getAllVideoContainsArtists(): Flow<List<VideoContainsArtist>>
    fun getAllAlbumContainsArtists(): Flow<List<AlbumContainsArtist>>
}