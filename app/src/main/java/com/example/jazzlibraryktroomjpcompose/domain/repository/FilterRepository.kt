package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Domain layer contract for filtering operations.
 * No implementation details – works only with domain models.
 */
interface FilterRepository {

    /**
     * Returns a reactive stream of filtered data based on the current filter path.
     */
    fun getFilteredDataFlow(filterPath: List<FilterPath>): Flow<FilteredData>

    /**
     * Fetches an artist's primary instrument (ID and name).
     * Used by the orchestrator when auto‑selecting an instrument after an artist is chosen.
     */
    suspend fun getArtistInstrument(artistId: Int): Pair<Int, String>?

    data class FilteredData(
        val videos: List<Video>,
        val albums: List<Album>,
        val artists: List<Artist>,
        val instruments: List<Instrument>,
        val durations: List<Duration>,
        val types: List<Type>,
        val filterPath: List<FilterPath>
    )
}