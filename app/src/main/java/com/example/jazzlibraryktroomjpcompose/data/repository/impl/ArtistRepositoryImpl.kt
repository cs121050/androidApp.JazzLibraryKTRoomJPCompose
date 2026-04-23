package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : ArtistRepository {

    // Basic
    override fun getAllArtists(): Flow<List<Artist>> =
        database.artistDao().getAllArtists().map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override fun getAllArtistsWithVideoCount(): Flow<List<Artist>> =
        database.artistDao().getAllArtistsWithVideoCount()
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    override suspend fun insertArtist(artist: Artist) {
        database.artistDao().insertArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun insertAllArtists(artists: List<Artist>) {
        database.artistDao().insertAllArtists(artists.map { ArtistMapper.toEntity(it) })
    }

    override suspend fun updateArtist(artist: Artist) {
        database.artistDao().updateArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun deleteArtist(artist: Artist) {
        database.artistDao().deleteArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun deleteAllArtists() {
        database.artistDao().deleteAllArtists()
    }

    override suspend fun updateAllEmbedableVideoCounts() {
        database.artistDao().updateAllEmbedableVideoCounts()
    }

    override fun getArtistById(id: Int): Flow<Artist?> =
        database.artistDao().getArtistById(id).map { it?.let { ArtistMapper.toDomain(it) } }

    override fun getArtistByName(query: String): Flow<List<Artist>> =
        database.artistDao().getArtistByName(query).map { entities -> entities.map { ArtistMapper.toDomain(it) } }


    override fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrument(instrumentId).map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override fun getArtistsByRank(rankId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByRank(rankId).map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    // With video count – single filter
    override fun getArtistsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrumentWithVideoCount(instrumentId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    override fun getArtistsByTypeWithVideoCount(typeId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByTypeWithVideoCount(typeId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    override fun getArtistsByDurationWithVideoCount(durationId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByDurationWithVideoCount(durationId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    // With video count – two filters
    override fun getArtistsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrumentAndTypeWithVideoCount(instrumentId, typeId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    override fun getArtistsByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrumentAndDurationWithVideoCount(instrumentId, durationId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    override fun getArtistsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByTypeAndDurationWithVideoCount(typeId, durationId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    // With video count – three filters
    override fun getArtistsByInstrumentAndTypeAndDurationWithVideoCount(
        instrumentId: Int, typeId: Int, durationId: Int
    ): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrumentAndTypeAndDurationWithVideoCount(instrumentId, typeId, durationId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    // Flexible filters (returning Artist)
    override fun getArtistsByMultipleFilters(
        instrumentId: Int, typeId: Int, durationId: Int
    ): Flow<List<Artist>> =
        database.artistDao().getArtistsByMultipleFilters(instrumentId, typeId, durationId)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    // Flexible filters (returning ArtistWithVideoCount)
    override fun getArtistsWithVideoCountByMultipleFilters(
        instrumentId: Int, typeId: Int, durationId: Int
    ): Flow<List<Artist>> =
        database.artistDao().getArtistsWithVideoCountByMultipleFilters(instrumentId, typeId, durationId)
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }


    override fun searchArtists(query: String): Flow<List<Artist>> = getArtistByName(query)

    override suspend fun refreshArtists(): Result<Unit> = Result.success(Unit)

    override suspend fun syncArtist(artistId: Int): Result<Artist> =
        Result.failure(UnsupportedOperationException("Remote sync not implemented"))

    override suspend fun fetchAndCacheArtists(): Result<List<Artist>> =
        Result.failure(UnsupportedOperationException("Remote fetch not implemented"))

}