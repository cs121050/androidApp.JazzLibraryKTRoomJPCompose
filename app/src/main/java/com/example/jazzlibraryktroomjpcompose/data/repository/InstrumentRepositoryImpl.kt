package com.example.jazzlibraryktroomjpcompose.data.repository


import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.InstrumentDao
import com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper
import com.example.jazzlibraryktroomjpcompose.data.remote.api.JazzApiService
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import com.example.jazzlibraryktroomjpcompose.domain.repository.InstrumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InstrumentRepositoryImpl @Inject constructor(
    private val database: JazzDatabase,
    private val apiService: JazzApiService
) : InstrumentRepository {

    private val instrumentDao: InstrumentDao = database.instrumentDao()

    override fun getAllInstruments(): Flow<List<Instrument>> {
        return instrumentDao.getAllInstruments()
            .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }
    }

    override fun getInstrumentById(id: Int): Flow<Instrument?> {
        return instrumentDao.getInstrumentById(id)
            .map { entity -> entity?.let { InstrumentMapper.toDomain(it) } }
    }

    override fun searchInstruments(query: String): Flow<List<Instrument>> {
        return instrumentDao.getInstrumentByName(query)
            .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }
    }

    override suspend fun saveInstrument(instrument: Instrument) {
        val entity = InstrumentMapper.toEntity(instrument)
        instrumentDao.insertInstrument(entity)
    }

    override suspend fun deleteInstrument(instrument: Instrument) {
        val entity = InstrumentMapper.toEntity(instrument)
        instrumentDao.deleteInstrument(entity)
    }

    override suspend fun refreshInstruments(): Result<Unit> {
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

    override suspend fun syncInstrument(instrumentId: Int): Result<Instrument> {
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

    override suspend fun fetchAndCacheInstruments(): Result<List<Instrument>> {
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