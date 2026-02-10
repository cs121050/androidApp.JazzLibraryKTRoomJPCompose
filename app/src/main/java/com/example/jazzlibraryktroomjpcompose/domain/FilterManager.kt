package com.example.jazzlibraryktroomjpcompose.domain

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterManager @Inject constructor(
    private val database: JazzDatabase
) {

    data class FilteredData(
        val videos: List<com.example.jazzlibraryktroomjpcompose.domain.models.Video>,
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

            // Get filtered videos
            val videosFlow = database.videoDao().getVideosByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0
            )

            // Get filtered artists
            val artistsFlow = database.artistDao().getArtistsByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )

            // Get filtered instruments
            val instrumentsFlow = database.instrumentDao().getInstrumentsByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )

            // Get filtered durations
            val durationsFlow = database.durationDao().getDurationsByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )

            // Get filtered types
            val typesFlow = database.typeDao().getTypesByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )
            // Get      artists WITH COUNT - using existing query
            val artistsFlowWithCount = database.artistDao().getArtistsWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )
            // Get instruments WITH COUNT - add this new method to your DAO
            val instrumentsFlowWithCount = database.instrumentDao().getInstrumentsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )
            // Get duration WITH COUNT - add this new method to your DAO
            val durationsFlowWithCount = database.durationDao().getDurationsWithVideoCountByMultipleFilters(
                typeId = typeFilter?.entityId ?: 0,
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )
            // Get type WITH COUNT - add this new method to your DAO
            val typesFlowWithCount = database.typeDao().getTypesWithVideoCountByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0,
                durationId = durationFilter?.entityId ?: 0
            )


            // Combine all flows
            combine(
                videosFlow,
                artistsFlowWithCount,//artistsFlow,
                instrumentsFlowWithCount,//instrumentsFlow,
                durationsFlowWithCount,//durationsFlow,
                typesFlowWithCount//typesFlow
            ) { videos, artists, instruments, durations, types ->
                FilteredData(
                    videos = videos.map { com.example.jazzlibraryktroomjpcompose.data.mappers.VideoMapper.toDomain(it) },
                    artists = artists.map { com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper.toDomainWithCount(it) },
                    instruments = instruments.map { com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper.toDomainWithCount(it) },
                    durations = durations.map { com.example.jazzlibraryktroomjpcompose.data.mappers.DurationMapper.toDomainWithCount(it) },
                    types = types.map { com.example.jazzlibraryktroomjpcompose.data.mappers.TypeMapper.toDomainWithCount(it) },
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
            // Check if chip is already selected (deselect case)
            currentFilterPath.any { it.categoryId == selectedCategoryId && it.entityId == selectedEntityId } -> {
                // Remove this chip
                val newPath = currentFilterPath.filterNot {
                    it.categoryId == selectedCategoryId && it.entityId == selectedEntityId
                }

                // If we're deselecting an instrument, also remove any artist
                if (selectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
                    newPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
                } else {
                    newPath
                }
            }

            // Check if there's already a chip in this category (replace case)
            currentFilterPath.any { it.categoryId == selectedCategoryId } -> {
                // Remove ALL existing chips in this category and add the new one
                val filteredPath = currentFilterPath.filterNot { it.categoryId == selectedCategoryId }

                // If selecting an instrument, also remove any artist
                val pathWithoutArtist = if (selectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
                    filteredPath.filterNot { it.categoryId == FilterPath.CATEGORY_ARTIST }
                } else {
                    filteredPath
                }
                pathWithoutArtist + FilterPath(
                    categoryId = selectedCategoryId,
                    entityId = selectedEntityId,
                    entityName = selectedEntityName
                )
            }

            // New chip selection
            else -> {
                val newFilterPath = currentFilterPath.toMutableList()
                newFilterPath.add(
                    FilterPath(
                        categoryId = selectedCategoryId,
                        entityId = selectedEntityId,
                        entityName = selectedEntityName
                    )
                )

                // Special rule: If artist is selected and no instrument is selected,
                // automatically select the artist's instrument
                if (selectedCategoryId == FilterPath.CATEGORY_ARTIST &&
                    !currentFilterPath.any { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }) {

                    val artist = database.artistDao().getArtistById(selectedEntityId).firstOrNull()
                    artist?.let {
                        // Check if we haven't already added this instrument
                        if (!newFilterPath.any { filter ->
                                filter.categoryId == FilterPath.CATEGORY_INSTRUMENT &&
                                        filter.entityId == it.instrumentId
                            }) {
                            newFilterPath.add(
                                FilterPath(
                                    categoryId = FilterPath.CATEGORY_INSTRUMENT,
                                    entityId = it.instrumentId,
                                    entityName = database.instrumentDao().getInstrumentById(it.instrumentId)
                                        .firstOrNull()?.name ?: "Unknown"
                                )
                            )
                        }
                    }
                }

                // NEW RULE: If instrument is selected, remove any existing artist
                if (selectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
                    newFilterPath.removeIf { it.categoryId == FilterPath.CATEGORY_ARTIST }
                }
                newFilterPath.toList()
            }
        }
        return result.distinctBy { it.categoryId }  // Deduplicate before returning
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