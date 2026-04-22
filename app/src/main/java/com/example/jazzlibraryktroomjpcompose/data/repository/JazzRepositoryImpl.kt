package com.example.jazzlibraryktroomjpcompose.data.repository


import android.util.Log
import androidx.room.withTransaction
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumContainsArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.SongRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoContainsArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toDurationEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toInstrumentEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toQuoteEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toTypeEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toSongEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumEntities
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class JazzRepositoryImpl(
    private val database: JazzDatabase
) {

    private val apiService = RetrofitClient.jazzApiService

    suspend fun checkApiConnectivity(): Boolean {
        return try {
            // Call the lightweight endpoint – if it succeeds without exception, API is reachable
            apiService.getApiStatus()
            true
        } catch (e: Exception) {
            println("API connectivity check failed: ${e.message}")
            false
        }
    }

    suspend fun loadBootstrapData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("JazzRepo", "loadBootstrapData: Starting API call")
            val response = apiService.getBootstrapData()
            Log.d("JazzRepo", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("JazzRepo", "Body received, size: ...")
                val bootstrapData = response.body()!!

                // Convert remote models to Room entities
                val instruments = bootstrapData.instrumentList.toInstrumentEntities()
                val types = bootstrapData.typeList.toTypeEntities()
                val durations = bootstrapData.durationList.toDurationEntities()
                val videos = bootstrapData.videoList.toVideoEntities()
                val artists = bootstrapData.artistList.toArtistEntities()
                val quotes = bootstrapData.quoteList.toQuoteEntities()
                val videoContainsArtists = bootstrapData.videoContainsArtistList.toVideoContainsArtistEntities()
                val albums = bootstrapData.albumList.toAlbumEntities()
                val songs = bootstrapData.songList.toSongEntities()
                val albumContainsArtists = bootstrapData.albumContainsArtistList.toAlbumContainsArtistEntities()

                // Use withTransaction which supports suspend functions
                //    withTransaction: If any insert fails → ALL changes are rolled back
                //    Prevents partial/corrupted data in database
                database.withTransaction {
                    // Clear existing data
                    clearAllTablesWithinTransaction()

                    // Insert all data
                    insertAllDataWithinTransaction(
                        instruments, types, durations, videos,
                        artists, quotes, videoContainsArtists,
                        albums, songs, albumContainsArtists
                    )

                    updateArtistsEmbedableVideoCounts()
                }

                Result.success(Unit)
            } else {
                Log.e("JazzRepo", "API error: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to load data: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("JazzRepo", "Exception in loadBootstrapData", e)
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
        database.albumDao().deleteAllAlbums()
        database.songDao().deleteAllSongs()
        database.albumContainsArtistDao().deleteAllAlbumContainsArtists()
    }

    private suspend fun insertAllDataWithinTransaction(
        instruments: List<InstrumentRoomEntity>,
        types: List<TypeRoomEntity>,
        durations: List<DurationRoomEntity>,
        videos: List<VideoRoomEntity>,
        artists: List<ArtistRoomEntity>,
        quotes: List<QuoteRoomEntity>,
        videoContainsArtists: List<VideoContainsArtistRoomEntity>,
        albums: List<AlbumRoomEntity>,
        songs: List<SongRoomEntity>,
        albumContainsArtists: List<AlbumContainsArtistRoomEntity>
    ) {
        database.instrumentDao().insertAllInstruments(instruments)
        database.typeDao().insertAllTypes(types)
        database.durationDao().insertAllDurations(durations)
        database.artistDao().insertAllArtists(artists)
        database.videoDao().insertAllVideos(videos)
        database.quoteDao().insertAllQuotes(quotes)
        database.videoContainsArtistDao().insertAllVideoContainsArtists(videoContainsArtists)
        database.albumDao().insertAllAlbums(albums)
        database.songDao().insertAllSongs(songs)
        database.albumContainsArtistDao().insertAllAlbumContainsArtists(albumContainsArtists)
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        database.videoDao().getCount() == 0
    }

    private suspend fun updateArtistsEmbedableVideoCounts() {
        database.artistDao().updateAllEmbedableVideoCounts()
    }
}