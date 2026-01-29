package com.example.jazzlibraryktroomjpcompose.domain

import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
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
                durationId = durationFilter?.entityId ?: 0,
                artistId = artistFilter?.entityId ?: 0
            )

            // Get filtered durations
            val durationsFlow = database.durationDao().getDurationsByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0,
                typeId = typeFilter?.entityId ?: 0
            )

            // Get filtered types
            val typesFlow = database.typeDao().getTypesByMultipleFilters(
                instrumentId = instrumentFilter?.entityId ?: 0
            )

            // Combine all flows
            combine(
                videosFlow,
                artistsFlow,
                instrumentsFlow,
                durationsFlow,
                typesFlow
            ) { videos, artists, instruments, durations, types ->
                FilteredData(
                    videos = videos.map { com.example.jazzlibraryktroomjpcompose.data.mappers.VideoMapper.toDomain(it) },
                    artists = artists.map { com.example.jazzlibraryktroomjpcompose.data.mappers.ArtistMapper.toDomain(it) },
                    instruments = instruments.map { com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper.toDomain(it) },
                    durations = durations.map { com.example.jazzlibraryktroomjpcompose.data.mappers.DurationMapper.toDomain(it) },
                    types = types.map { com.example.jazzlibraryktroomjpcompose.data.mappers.TypeMapper.toDomain(it) },
                    filterPath = filterPath
                )
            }.collect { emit(it) }
        }
    }

    suspend fun handleChipSelection(
        currentFilterPath: List<FilterPath>,
        selectedCategoryId: Int,
        selectedEntityId: Int,
        selectedEntityName: String
    ): List<FilterPath> {
        return when {
            // Check if chip is already selected (deselect case)
            currentFilterPath.any { it.categoryId == selectedCategoryId && it.entityId == selectedEntityId } -> {
                // Remove this chip
                currentFilterPath.filterNot {
                    it.categoryId == selectedCategoryId && it.entityId == selectedEntityId
                }
            }

            // Check if there's already a chip in this category (replace case)
            currentFilterPath.any { it.categoryId == selectedCategoryId } -> {
                // Replace the existing chip in this category
                currentFilterPath.map {
                    if (it.categoryId == selectedCategoryId) {
                        FilterPath(
                            categoryId = selectedCategoryId,
                            entityId = selectedEntityId,
                            entityName = selectedEntityName
                        )
                    } else {
                        it
                    }
                }
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
                    !newFilterPath.any { it.categoryId == FilterPath.CATEGORY_INSTRUMENT }) {

                    val artist = database.artistDao().getArtistById(selectedEntityId).firstOrNull()
                    artist?.let {
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

                newFilterPath.toList()
            }
        }
    }

    suspend fun handleChipDeselection(
        currentFilterPath: List<FilterPath>,
        deselectedCategoryId: Int,
        deselectedEntityId: Int
    ): List<FilterPath> {
        val newFilterPath = currentFilterPath.toMutableList()

        // Remove the deselected chip
        newFilterPath.removeAll {
            it.categoryId == deselectedCategoryId && it.entityId == deselectedEntityId
        }

        // Special rule: If instrument is deselected and there's an artist with that instrument,
        // also remove the artist
        if (deselectedCategoryId == FilterPath.CATEGORY_INSTRUMENT) {
            // Find artists that use this instrument
            val artistsWithInstrument = database.artistDao().getArtistsByInstrument(deselectedEntityId)
                .firstOrNull() ?: emptyList()

            // Remove any artist that uses this instrument
            artistsWithInstrument.forEach { artist ->
                newFilterPath.removeAll {
                    it.categoryId == FilterPath.CATEGORY_ARTIST && it.entityId == artist.id
                }
            }
        }

        return newFilterPath.toList()
    }
}