package com.example.jazzlibraryktroomjpcompose.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.*
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumContainsArtistEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toAlbumEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toArtistEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toDurationEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toDurationEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toInstrumentEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toInstrumentEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toQuoteEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toSongEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toSongEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toTypeEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toTypeEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoContainsArtistEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoContainsArtistEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoEntities
import com.example.jazzlibraryktroomjpcompose.data.mappers.RemoteToEntityMappers.toVideoEntity
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteAlbum
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteAlbumContainsArtist
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteArtist
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteDuration
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteInstrument
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteSong
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteType
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteVideo
import com.example.jazzlibraryktroomjpcompose.data.remote.models.RemoteVideoContainsArtist
import com.example.jazzlibraryktroomjpcompose.domain.models.*
import com.example.jazzlibraryktroomjpcompose.domain.repository.FilterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import com.example.jazzlibraryktroomjpcompose.data.remote.models.SyncPage
import com.google.gson.JsonObject
import com.google.gson.Gson


@Singleton
class JazzRepositoryImpl(
    private val database: JazzDatabase
) : FilterRepository {

    //TODO that is a design flaw , try injecting the API servive , like you did for jazzdatabase.
    // otherwise the code will be less testable and harder to read, creating unit test for this here is hard...
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

    suspend fun refreshDataPrimary(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i("Sync", "refreshDataPrimary: START")
        try {
            Log.d("Sync", "refreshDataPrimary: fetching manifest")
            val manifestResponse = apiService.getSyncManifest()
            if (!manifestResponse.isSuccessful || manifestResponse.body() == null) {
                Log.e("Sync", "manifest failed: code=${manifestResponse.code()} msg=${manifestResponse.message()}")
                return@withContext Result.failure(Exception("Manifest fetch failed"))
            }

            val manifest = manifestResponse.body()!!
            Log.i("Sync", "manifest OK, ${manifest.size} resources: ${manifest.joinToString { "${it.name}(${it.count})" }}")

            for (resource in manifest) {
                Log.d("Sync", "resource ${resource.name}: START (expected ${resource.count}, path='${resource.path}')")
                var cursor: String? = null
                var received = 0
                var pageIdx = 0

                while (true) {
                    val pageResponse = apiService.getSyncPage(
                        resource = resource.path.removePrefix("/sync/"),
                        cursor = cursor,
                        limit = 1000
                    )
                    Log.d("Sync", "  ${resource.name} page#$pageIdx cursor=$cursor → code=${pageResponse.code()}, hasBody=${pageResponse.body() != null}")

                    if (!pageResponse.isSuccessful || pageResponse.body() == null) {
                        Log.e("Sync", "  ${resource.name} page#$pageIdx FAILED code=${pageResponse.code()}")
                        return@withContext Result.failure(
                            Exception("Page fetch failed: ${resource.name}")
                        )
                    }

                    val page = pageResponse.body()!!
                    Log.d("Sync", "  ${resource.name} page#$pageIdx items=${page.items.size} hasMore=${page.hasMore} nextCursor=${page.nextCursor}")

                    if (page.hasMore && page.nextCursor == null) {
                        Log.e("Sync", "  contract violation: hasMore=true nextCursor=null on ${resource.name}")
                        return@withContext Result.failure(
                            Exception("Contract violation: hasMore=true but nextCursor=null")
                        )
                    }
                    if (page.hasMore && page.nextCursor == cursor) {
                        Log.e("Sync", "  contract violation: cursor stuck on ${resource.name}")
                        return@withContext Result.failure(
                            Exception("Contract violation: nextCursor did not advance")
                        )
                    }
                    if (page.hasMore && page.items.isEmpty()) {
                        Log.e("Sync", "  contract violation: empty page with hasMore=true on ${resource.name}")
                        return@withContext Result.failure(
                            Exception("Contract violation: empty page while hasMore=true")
                        )
                    }

                    upsertPageIntoDb(resource.name, page.items)
                    received += page.items.size
                    pageIdx++

                    if (!page.hasMore) break
                    cursor = page.nextCursor
                }

                Log.d("Sync", "resource ${resource.name}: DONE received=$received expected=${resource.count}")
                if (received.toLong() != resource.count) {
                    Log.e("Sync", "incomplete sync on ${resource.name}: received=$received expected=${resource.count}")
                    return@withContext Result.failure(
                        Exception("Incomplete sync for ${resource.name}: received=$received expected=${resource.count}")
                    )
                }
            }

            Log.i("Sync", "refreshDataPrimary: recomputing artist video counts")
            database.withTransaction {
                updateArtistsEmbedableVideoCounts()
            }

            Log.i("Sync", "refreshDataPrimary: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Sync", "refreshDataPrimary: EXCEPTION → falling back to bootstrap", e)
            loadBootstrapData().also { result ->
                Log.i("Sync", "bootstrap fallback result: ${if (result.isSuccess) "SUCCESS" else "FAILURE ${result.exceptionOrNull()?.message}"}")
            }
        }
    }

    suspend fun loadBootstrapData(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i("Bootstrap", "loadBootstrapData: START")
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
                Log.i("Bootstrap", "loadBootstrapData: SUCCESS")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to load data: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("Bootstrap", "loadBootstrapData: EXCEPTION", e)
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

    private suspend fun upsertPageIntoDb(resourceName: String, items: List<JsonObject>) {
        database.withTransaction {
            when (resourceName) {
                "types" -> database.typeDao().insertAllTypes(
                    items.map { it.toRemoteModel<RemoteType>().toTypeEntity() }
                )
                "durations" -> database.durationDao().insertAllDurations(
                    items.map { it.toRemoteModel<RemoteDuration>().toDurationEntity() }
                )
                "instruments" -> database.instrumentDao().insertAllInstruments(
                    items.map { it.toRemoteModel<RemoteInstrument>().toInstrumentEntity() }
                )
                "artists" -> database.artistDao().insertAllArtists(
                    items.map { it.toRemoteModel<RemoteArtist>().toArtistEntity() }
                )
                "albums" -> database.albumDao().insertAllAlbums(
                    items.map { it.toRemoteModel<RemoteAlbum>().toAlbumEntity() }
                )
                "songs" -> database.songDao().insertAllSongs(
                    items.map { it.toRemoteModel<RemoteSong>().toSongEntity() }
                )
                "videos" -> database.videoDao().insertAllVideos(
                    items.map { it.toRemoteModel<RemoteVideo>().toVideoEntity() }
                )
                "album-artists" -> database.albumContainsArtistDao().insertAllAlbumContainsArtists(
                    items.map { it.toRemoteModel<RemoteAlbumContainsArtist>().toAlbumContainsArtistEntity() }
                )
                "video-artists" -> database.videoContainsArtistDao().insertAllVideoContainsArtists(
                    items.map { it.toRemoteModel<RemoteVideoContainsArtist>().toVideoContainsArtistEntity() }
                )
                else -> Unit
            }
        }
    }

    private suspend fun updateArtistsEmbedableVideoCounts() {
        database.artistDao().updateAllEmbedableVideoCounts()
    }

    // ========== FILTER REPOSITORY IMPLEMENTATION ==========
    // here dependent of the list of applied filters,, load all the resources form all duration,artist,video etc
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






    private val syncGson = Gson()

    private inline fun <reified T> JsonObject.toRemoteModel(): T =
        syncGson.fromJson(this, T::class.java)

}