package com.example.jazzlibraryktroomjpcompose.data.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.ArtistDao
import com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArtistRepositoryImpl @Inject constructor(
    private val database: JazzDatabase,
    private val apiService: RetrofitClient
) : ArtistRepository {

    private val artistDao: ArtistDao = database.artistDao()

    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists()
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
    }

    override fun getArtistById(id: Int): Flow<Artist?> {
        return artistDao.getArtistById(id)
            .map { entity -> entity?.let { ArtistMapper.toDomain(it) } }
    }

    override fun searchArtists(query: String): Flow<List<Artist>> {
        return artistDao.getArtistByName(query)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
    }

    override fun getArtistsByInstrument(instrumentId: Int): Flow<List<Artist>> {
        return artistDao.getArtistsByInstrument(instrumentId)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
    }

    override fun getArtistsByRank(rankId: Int): Flow<List<Artist>> {
        return artistDao.getArtistsByRank(rankId)
            .map { entities -> entities.map { ArtistMapper.toDomain(it) } }
    }

    override suspend fun saveArtist(artist: Artist) {
        val entity = ArtistMapper.toEntity(artist)
        artistDao.insertArtist(entity)
    }

    override suspend fun deleteArtist(artist: Artist) {
        val entity = ArtistMapper.toEntity(artist)
        artistDao.deleteArtist(entity)
    }



    override suspend fun refreshArtists(): Result<Unit> {
//        return try {
//            // Fetch from API
//            val response = apiService.getAllArtists()
//
//            if (response.isSuccessful) {
//                val artists = response.body() ?: emptyList()
//
//                // Convert to domain models
//                val domainArtists = artists.map { ArtistMapper.toDomain(it) }
//
//                // Convert to entities and save to local DB
//                val entities = domainArtists.map { ArtistMapper.toEntity(it) }
//                artistDao.insertAllArtists(entities)
//
//                Result.success(Unit)
//            } else {
//                Result.failure(Exception("Failed to fetch artists: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
        return TODO("Provide the return value")
    }

    override suspend fun syncArtist(artistId: Int): Result<Artist> {
//        return try {
//            val response = apiService.getArtistById(artistId)
//
//            if (response.isSuccessful) {
//                val artistResponse = response.body()
//                if (artistResponse != null) {
//                    val domainArtist = ArtistMapper.toDomain(artistResponse)
//                    val entity = ArtistMapper.toEntity(domainArtist)
//
//                    artistDao.insertArtist(entity)
//                    Result.success(domainArtist)
//                } else {
//                    Result.failure(Exception("Artist not found"))
//                }
//            } else {
//                Result.failure(Exception("Failed to sync artist: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
        return TODO("Provide the return value")
    }

    override suspend fun fetchAndCacheArtists(): Result<List<Artist>> {
//        return try {
//            val response = apiService.getAllArtists()
//
//            if (response.isSuccessful) {
//                val artists = response.body() ?: emptyList()
//                val domainArtists = artists.map { ArtistMapper.toDomain(it) }
//                val entities = domainArtists.map { ArtistMapper.toEntity(it) }
//
//                // Clear and insert new data
//                artistDao.deleteAllArtists()
//                artistDao.insertAllArtists(entities)
//
//                Result.success(domainArtists)
//            } else {
//                Result.failure(Exception("API error: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }

        return TODO("Provide the return value")
    }
}