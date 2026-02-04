package com.example.jazzlibraryktroomjpcompose.data.repository


import androidx.room.withTransaction
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toDurationEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toInstrumentEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toQuoteEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toTypeEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoEntities
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class JazzRepositoryImpl(
    private val database: JazzDatabase
) {

    private val apiService = RetrofitClient.jazzApiService

    suspend fun loadBootstrapData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBootstrapData()

            if (response.isSuccessful && response.body() != null) {
                val bootstrapData = response.body()!!

                // Convert remote models to Room entities
                val instruments = bootstrapData.instrumentList.toInstrumentEntities()
                val types = bootstrapData.typeList.toTypeEntities()
                val durations = bootstrapData.durationList.toDurationEntities()
                val videos = bootstrapData.videoList.toVideoEntities()
                val artists = bootstrapData.artistList.toArtistEntities()
                val quotes = bootstrapData.quoteList.toQuoteEntities()
                val videoContainsArtists = bootstrapData.videoContainsArtistList.toVideoContainsArtistEntities()

                // Use withTransaction which supports suspend functions
                database.withTransaction {
                    // Clear existing data
                    clearAllTablesWithinTransaction()

                    // Insert all data
                    insertAllDataWithinTransaction(
                        instruments, types, durations, videos,
                        artists, quotes, videoContainsArtists
                    )
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to load data: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun clearAllTablesWithinTransaction() {
        database.videoContainsArtistDao().deleteAllVideoContainsArtists()
        database.quoteDao().deleteAllQuotes()
        database.videoDao().deleteAllVideos()
        database.artistDao().deleteAllArtists()
        database.durationDao().deleteAllDurations()
        database.typeDao().deleteAllTypes()
        database.instrumentDao().deleteAllInstruments()
    }

    private suspend fun insertAllDataWithinTransaction(
        instruments: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity>,
        types: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity>,
        durations: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity>,
        videos: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoRoomEntity>,
        artists: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity>,
        quotes: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity>,
        videoContainsArtists: List<com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoContainsArtistRoomEntity>
    ) {
        database.instrumentDao().insertAllInstruments(instruments)
        database.typeDao().insertAllTypes(types)
        database.durationDao().insertAllDurations(durations)
        database.artistDao().insertAllArtists(artists)
        database.videoDao().insertAllVideos(videos)
        database.quoteDao().insertAllQuotes(quotes)
        database.videoContainsArtistDao().insertAllVideoContainsArtists(videoContainsArtists)
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        database.videoDao().getCount() == 0
    }
}