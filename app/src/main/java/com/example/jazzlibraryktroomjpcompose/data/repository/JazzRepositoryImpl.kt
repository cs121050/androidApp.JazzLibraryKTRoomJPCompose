package com.example.jazzlibraryktroomjpcompose.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toDurationEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toInstrumentEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toQuoteEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toSongEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toTypeEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoEntities
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
import com.example.jazzlibraryktroomjpcompose.domain.models.*
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class JazzRepositoryImpl(
    private val database: JazzDatabase
) : FilterRepository {

    private val apiService = RetrofitClient.jazzApiService

    // ========== BOOTSTRAP & INITIALISATION (keep your existing code) ==========

    suspend fun checkApiConnectivity(): Boolean {
        return try {
            val response = apiService.getApiStatus()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("JazzRepository", "API connectivity check failed", e)
            false
        }
    }

    suspend fun loadBootstrapData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBootstrapData()
            if (response.isSuccessful && response.body() != null) {
                val bootstrapData = response.body()!!
                // Convert to entities (use your existing mappers)
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

                database.withTransaction {
                    clearAllTablesWithinTransaction()
                    insertAllDataWithinTransaction(
                        instruments, types, durations, videos,
                        artists, quotes, videoContainsArtists,
                        albums, songs, albumContainsArtists
                    )
                    updateArtistsEmbedableVideoCounts()
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to load data: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        database.videoDao().getCount() == 0
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

    private suspend fun updateArtistsEmbedableVideoCounts() {
        database.artistDao().updateAllEmbedableVideoCounts()
    }

    // ========== FILTER REPOSITORY IMPLEMENTATION ==========

    override fun getFilteredDataFlow(filterPath: List<FilterPath>): Flow<FilterRepository.FilteredData> {
        return flow {
            val instrumentFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }
            val artistFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_ARTIST }
            val durationFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_DURATION }
            val typeFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_TYPE }
            val searchFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_SEARCH }
            val searchMode = searchFilter?.entityId ?: -1
            val searchQuery = searchFilter?.entityName ?: ""

            Log.d("JazzRepositoryImpl", "🔍 Search mode=$searchMode, query='$searchQuery'")

            // 1. VIDEOS: apply search only if mode == 0
            val videoSearchQuery = if (searchMode == 0) searchQuery else ""
            Log.d("JazzRepositoryImpl", "🎬 Video search query='$videoSearchQuery'")
            val videosFlow = database.videoDao().getVideosByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                searchQuery = videoSearchQuery
            )

            // 2. ALBUMS: apply search only if mode == 2
            val albumSearchQuery = if (searchMode == 2) searchQuery else ""
            Log.d("JazzRepositoryImpl", "💿 Album search query='$albumSearchQuery'")
            val albumsFlow = if (artistFilter != null) {
                database.albumDao().getAlbumsByArtistAndInstrumentWithMainFlag(
                    artistId = artistFilter.entityId,
                    instrumentId = instrumentFilter?.entityId ?: 0,
                    searchQuery = albumSearchQuery
                )
            } else {
                database.albumDao().getAlbumByMultipleFilters(
                    instrumentId = instrumentFilter?.entityId ?: 0,
                    artistId = 0,
                    searchQuery = albumSearchQuery
                )
            }

            // 3. ARTISTS: apply search only if mode == 1
            val artistSearchQuery = if (searchMode == 1) searchQuery else ""
            Log.d("JazzRepositoryImpl", "🎤 Artist search query='$artistSearchQuery'")
            val artistsFlowWithCount = database.artistDao().getArtistsWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0,
                searchQuery = artistSearchQuery
            )

            // 4. INSTRUMENTS (no search)
            val instrumentsFlowWithCount = database.instrumentDao().getInstrumentsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )

            // 5. DURATIONS (no search)
            val durationsFlowWithCount = database.durationDao().getDurationsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )

            // 6. TYPES (no search)
            val typesFlowWithCount = database.typeDao().getTypesWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )

            combine(
                videosFlow,
                albumsFlow,
                artistsFlowWithCount,
                instrumentsFlowWithCount,
                durationsFlowWithCount,
                typesFlowWithCount
            ) { values ->
                val videos = values[0] as List<VideoRoomEntity>
                val albums = values[1] as List<AlbumWithIsMainFlag>
                val artists = values[2] as List<ArtistWithVideoCount>
                val instruments = values[3] as List<InstrumentWithVideoCount>
                val durations = values[4] as List<DurationWithVideoCount>
                val types = values[5] as List<TypeWithVideoCount>

                Log.d("JazzRepositoryImpl", "📹 Videos after filter: ${videos.size}")
                Log.d("JazzRepositoryImpl", "💿 Albums after filter: ${albums.size}")
                Log.d("JazzRepositoryImpl", "🎤 Artists after filter: ${artists.size}")

                FilterRepository.FilteredData(
                    videos = videos.map { VideoMapper.toDomain(it) },
                    albums = albums.map { AlbumMapper.toDomainWithIsMainFlag(it) },
                    artists = artists.map { ArtistMapper.toDomainWithCount(it) },
                    instruments = instruments.map { InstrumentMapper.toDomainWithCount(it) },
                    durations = durations.map { DurationMapper.toDomainWithCount(it) },
                    types = types.map { TypeMapper.toDomainWithCount(it) },
                    filterPath = filterPath
                )
            }.collect { emit(it) }
        }
    }

    override suspend fun getArtistInstrument(artistId: Int): Pair<Int, String>? = withContext(Dispatchers.IO) {
        try {
            val artist = database.artistDao().getArtistById(artistId).firstOrNull()
            if (artist != null && artist.instrumentId != null && artist.instrumentId > 0) {
                val instrument = database.instrumentDao().getInstrumentById(artist.instrumentId).firstOrNull()
                if (instrument != null) {
                    return@withContext Pair(artist.instrumentId, instrument.name)
                }
            }
            null
        } catch (e: Exception) {
            Log.e("JazzRepository", "Error getting artist instrument", e)
            null
        }
    }
}