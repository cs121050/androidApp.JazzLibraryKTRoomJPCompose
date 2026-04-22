package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import kotlinx.coroutines.flow.Flow

// domain/repository/ArtistRepository.kt
interface ArtistRepository {
    // existing methods (keep them all)
    fun getAllArtists(): Flow<List<Artist>>
    fun getArtistById(id: Int): Flow<Artist?>
    fun searchArtists(query: String): Flow<List<Artist>>
    fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>>
    suspend fun saveArtist(artist: Artist)
    suspend fun deleteArtist(artist: Artist)
    suspend fun refreshArtists(): Result<Unit>
    suspend fun syncArtist(artistId: Int): Result<Artist>
    suspend fun fetchAndCacheArtists(): Result<List<Artist>>
    fun getArtistsByRank(rankId: Int): Flow<List<Artist>>

    // ✅ NEW methods needed by MainViewModel
    fun getAllArtistsWithVideoCount(): Flow<List<Artist>>
}