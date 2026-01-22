package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    // Local operations
    fun getAllArtists(): Flow<List<Artist>>
    fun getArtistById(id: Int): Flow<Artist?>
    fun searchArtists(query: String): Flow<List<Artist>>
    fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>>

    suspend fun saveArtist(artist: Artist)
    suspend fun deleteArtist(artist: Artist)

    // Remote operations
    suspend fun refreshArtists(): Result<Unit>
    suspend fun syncArtist(artistId: Int): Result<Artist>

    // Combined operations
    suspend fun fetchAndCacheArtists(): Result<List<Artist>>
}