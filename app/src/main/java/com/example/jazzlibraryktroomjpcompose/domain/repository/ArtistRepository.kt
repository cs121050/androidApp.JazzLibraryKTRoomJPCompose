package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    // Basic queries
    fun getAllArtists(): Flow<List<Artist>>
    fun getAllArtistsWithVideoCount(): Flow<List<Artist>>
    suspend fun insertArtist(artist: Artist)
    suspend fun insertAllArtists(artists: List<Artist>)
    suspend fun updateArtist(artist: Artist)
    suspend fun deleteArtist(artist: Artist)
    suspend fun deleteAllArtists()
    suspend fun updateAllEmbedableVideoCounts()

    // Single entity
    fun getArtistById(id: Int): Flow<Artist?>

    // Search & filters
    fun getArtistByName(query: String): Flow<List<Artist>>
    fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>>
    fun getArtistsByRank(rankId: Int): Flow<List<Artist>>

    // With video count – single filter
    fun getArtistsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Artist>>
    fun getArtistsByTypeWithVideoCount(typeId: Int): Flow<List<Artist>>
    fun getArtistsByDurationWithVideoCount(durationId: Int): Flow<List<Artist>>

    // With video count – two filters
    fun getArtistsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<Artist>>
    fun getArtistsByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<Artist>>
    fun getArtistsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<Artist>>

    // With video count – three filters
    fun getArtistsByInstrumentAndTypeAndDurationWithVideoCount(
        instrumentId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<Artist>>

    // Flexible filters (returning Artist)
    fun getArtistsByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        durationId: Int = 0
    ): Flow<List<Artist>>

    // Flexible filters (returning ArtistWithVideoCount)
    fun getArtistsWithVideoCountByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        durationId: Int = 0
    ): Flow<List<Artist>>



    // Search (alias for getArtistByName, or separate implementation)
    fun searchArtists(query: String): Flow<List<Artist>>

    // Remote sync operations (stubs for now)
    suspend fun refreshArtists(): Result<Unit>
    suspend fun syncArtist(artistId: Int): Result<Artist>
    suspend fun fetchAndCacheArtists(): Result<List<Artist>>
}