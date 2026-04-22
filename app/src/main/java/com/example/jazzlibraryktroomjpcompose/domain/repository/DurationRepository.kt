package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Duration
import kotlinx.coroutines.flow.Flow

interface DurationRepository {
    // Basic
    fun getAllDurations(): Flow<List<Duration>>
    fun getAllDurationsWithCount(): Flow<List<Duration>>
    suspend fun insertDuration(duration: Duration)
    suspend fun insertAllDurations(durations: List<Duration>)
    suspend fun updateDuration(duration: Duration)
    suspend fun deleteDuration(duration: Duration)
    suspend fun deleteAllDurations()

    // Single entity
    fun getDurationById(id: Int): Flow<Duration?>

    // Search
    fun searchDurations(query: String): Flow<List<Duration>>

    // With video count – single filter
    fun getDurationsByTypeWithVideoCount(typeId: Int): Flow<List<Duration>>
    fun getDurationsByArtistWithVideoCount(artistId: Int): Flow<List<Duration>>
    fun getDurationsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Duration>>

    // With video count – two filters
    fun getDurationsByTypeAndArtistWithVideoCount(typeId: Int, artistId: Int): Flow<List<Duration>>
    fun getDurationsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<Duration>>
    fun getDurationsByArtistAndInstrumentWithVideoCount(artistId: Int, instrumentId: Int): Flow<List<Duration>>

    // With video count – three filters
    fun getDurationsByInstrumentAndTypeAndArtistWithVideoCount(
        instrumentId: Int,
        typeId: Int,
        artistId: Int
    ): Flow<List<Duration>>

    // Flexible filters (returning Duration)
    fun getDurationsByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Duration>>

    // Flexible filters (returning DurationWithVideoCount)
    fun getDurationsWithVideoCountByMultipleFilters(
        instrumentId: Int = 0,
        typeId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Duration>>
}