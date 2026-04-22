// data/repository/impl/ArtistRepositoryImpl.kt
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

    // === Existing local methods (implemented) ===
    override fun getAllArtists(): Flow<List<Artist>> =
        database.artistDao().getAllArtists()
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override fun getArtistById(id: Int): Flow<Artist?> =
        database.artistDao().getArtistById(id)
            .map { entity -> entity?.let { ArtistMapper.toDomain(it) } }

    override fun searchArtists(query: String): Flow<List<Artist>> =
        database.artistDao().getArtistByName(query)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByInstrument(instrumentId)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override fun getArtistsByRank(rankId: Int): Flow<List<Artist>> =
        database.artistDao().getArtistsByRank(rankId)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }

    override suspend fun saveArtist(artist: Artist) {
        database.artistDao().insertArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun deleteArtist(artist: Artist) {
        database.artistDao().deleteArtist(ArtistMapper.toEntity(artist))
    }

    // === NEW method required by ViewModel ===
    override fun getAllArtistsWithVideoCount(): Flow<List<Artist>> =
        database.artistDao().getAllArtistsWithVideoCount()
            .map { entities -> entities.map { ArtistMapper.toDomainWithCount(it) } }

    // === Stubs for remote operations (not used yet) ===
    override suspend fun refreshArtists(): Result<Unit> = Result.success(Unit)
    override suspend fun syncArtist(artistId: Int): Result<Artist> =
        Result.failure(UnsupportedOperationException("Remote sync not implemented"))
    override suspend fun fetchAndCacheArtists(): Result<List<Artist>> =
        Result.failure(UnsupportedOperationException("Remote fetch not implemented"))
}