package com.example.jazzlibraryktroomjpcompose.domain

import android.util.Log
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.AlbumWithIsMainFlag
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.DurationWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeWithVideoCount
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.VideoRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.mappers.AlbumMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.DurationMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.TypeMapper
import com.example.jazzlibraryktroomjpcompose.data.mappers.VideoMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Album
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterManager @Inject constructor(
    private val database: JazzDatabase
) {

    data class FilteredData(
        val videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video>,
        val albums: List<Album>,
        val artists: List<com.example.jazzlibraryktroomjpcompose.domain.models.Artist>,
        val instruments: List<com.example.jazzlibraryktroomjpcompose.domain.models.Instrument>,
        val durations: List<com.example.jazzlibraryktroomjpcompose.domain.models.Duration>,
        val types: List<com.example.jazzlibraryktroomjpcompose.domain.models.Type>,
        val filterPath: List<FilterPath>
    )

    fun getFilteredDataFlow(filterPath: List<FilterPath>): Flow<FilteredData> {
        return flow {
            // Extract filter values from filter path
            val instrumentFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }
            val artistFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_ARTIST }
            val durationFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_DURATION }
            val typeFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_TYPE }
            val searchFilter = filterPath.find { it.categoryId == FilterPath.CATEGORY_SEARCH }
            val searchMode = searchFilter?.entityId ?: -1   // 0=video,1=artist,2=album
            val searchQuery = searchFilter?.entityName ?: ""
            Log.d("FilterManager", "🔍 Search mode=$searchMode, query='$searchQuery'")
            Log.d("FilterManager", "Search mode=$searchMode, query='$searchQuery'")


            // ──────────────────────────────────────────────────────────────
            // 1. VIDEOS: apply searchQuery only if mode == 0
            val videoSearchQuery = if (searchMode == 0) searchQuery else ""
            Log.d("FilterManager", "Video search query='$videoSearchQuery'")
            Log.d("FilterManager", "🎬 Video search query='$videoSearchQuery'")
            val videosFlow = database.videoDao().getVideosByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                searchQuery = videoSearchQuery
            )

            // ──────────────────────────────────────────────────────────────
            // 2. ALBUMS: apply searchQuery only if mode == 2
            val albumSearchQuery = if (searchMode == 2) searchQuery else ""
            Log.d("FilterManager", "album search query='$albumSearchQuery'")
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

            // ──────────────────────────────────────────────────────────────
            // 3. ARTISTS: apply searchQuery only if mode == 1
            val artistSearchQuery = if (searchMode == 1) searchQuery else ""
            Log.d("FilterManager", "artist search query='$artistSearchQuery'")
            val artistsFlowWithCount = database.artistDao().getArtistsWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0,
                searchQuery = artistSearchQuery
            )

            // ──────────────────────────────────────────────────────────────
            // 4. INSTRUMENTS: no search support
            val instrumentsFlowWithCount = database.instrumentDao().getInstrumentsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )

            // ──────────────────────────────────────────────────────────────
            // 5. DURATIONS: no search support
            val durationsFlowWithCount = database.durationDao().getDurationsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )

            // ──────────────────────────────────────────────────────────────
            // 6. TYPES: no search support
            val typesFlowWithCount = database.typeDao().getTypesWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )


            // Combine all flows
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

                FilteredData(
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
//TODO// separate bussiness logic from basic functionality
suspend fun handleChipSelection(
    currentFilterPath: List<FilterPath>,
    selectedCategoryId: Int,
    selectedEntityId: Int,
    selectedEntityName: String
): List<FilterPath> {

    val result = when {
        // Deselection case (chip already selected)
        currentFilterPath.any { it.categoryId == selectedCategoryId && it.entityId == selectedEntityId } -> {
            val newPath = currentFilterPath.filterNot {
                it.categoryId == selectedCategoryId && it.entityId == selectedEntityId
            }
            // If deselecting an instrument, also remove any artist
            if (selectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
                newPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
            } else {
                newPath
            }
        }

        // Selection case (new or replacement – same logic)
        else -> {
            // Remove all existing chips of the same category
            var filteredPath = currentFilterPath.filterNot { it.categoryId == selectedCategoryId }

            when (selectedCategoryId) {
                FilterPath.CATEGORY_ARTIST -> {
                    // 1. Remove any existing instrument filter
                    filteredPath = filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }

                    // 2. Get the artist's primary instrument
                    val artist = database.artistDao().getArtistById(selectedEntityId).firstOrNull()
                    val instrumentId = artist?.instrumentId
                    val instrumentName = if (instrumentId != null && instrumentId > 0) {
                        database.instrumentDao().getInstrumentById(instrumentId).firstOrNull()?.name ?: ""
                    } else ""

                    // 3. Build the new list: base + instrument (if valid) + artist
                    val newPath = filteredPath.toMutableList()
                    if (instrumentId != null && instrumentId > 0 && instrumentName.isNotBlank()) {
                        newPath.add(
                            FilterPath(
                                categoryId = FilterPath.CATEGORY_INSTRUMENT,
                                entityId = instrumentId,
                                entityName = instrumentName
                            )
                        )
                    }
                    newPath.add(
                        FilterPath(
                            categoryId = FilterPath.CATEGORY_ARTIST,
                            entityId = selectedEntityId,
                            entityName = selectedEntityName
                        )
                    )
                    newPath.toList()
                }

                FilterPath.CATEGORY_INSTRUMENT -> {
                    // When selecting an instrument, remove any existing artist
                    filteredPath = filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
                    filteredPath + FilterPath(
                        categoryId = selectedCategoryId,
                        entityId = selectedEntityId,
                        entityName = selectedEntityName
                    )
                }

                FilterPath.CATEGORY_SEARCH -> {
                    // Only one search chip allowed: remove any existing search chip,
                    // then add the new one.
                    val filteredWithoutSearch = filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_SEARCH }
                    filteredWithoutSearch + FilterPath(
                        categoryId = selectedCategoryId,
                        entityId = selectedEntityId,
                        entityName = selectedEntityName
                    )
                }

                else -> {
                    // For duration or type, just add the new chip
                    filteredPath + FilterPath(
                        categoryId = selectedCategoryId,
                        entityId = selectedEntityId,
                        entityName = selectedEntityName
                    )
                }
            }
        }
    }
    return result.distinctBy { it.categoryId }
}

    suspend fun handleChipDeselection(
        currentFilterPath: List<FilterPath>,
        categoryId: Int,
        entityId: Int
    ): List<FilterPath> {
        val result = when (categoryId) {
            FilterPath.CATEGORY_INSTRUMENT -> {
                // When instrument is deselected, also remove any artist
                currentFilterPath.filterNot { filter ->
                    filter.categoryId == categoryId ||  // Remove the instrument
                            filter.categoryId == FilterPath.CATEGORY_ARTIST  // Remove any artist
                }
            }

            FilterPath.CATEGORY_SEARCH -> {
                // Remove only the specific search chip (by entityId and entityName)
                currentFilterPath.filterNot {
                    it.categoryId == categoryId && it.entityId == entityId
                }
            }

            else -> {
                // For other categories, just remove the specific chip
                currentFilterPath.filterNot {
                    it.categoryId == categoryId && it.entityId == entityId
                }
            }
        }

        return result.distinctBy { it.categoryId }  // Deduplicate before returning
    }


}