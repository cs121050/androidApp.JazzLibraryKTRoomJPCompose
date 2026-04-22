package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import kotlinx.coroutines.flow.Flow

interface InstrumentRepository {
    // Basic
    fun getAllInstruments(): Flow<List<Instrument>>
    fun getAllInstrumentsWithArtistCount(): Flow<List<Instrument>>
    suspend fun insertInstrument(instrument: Instrument)
    suspend fun insertAllInstruments(instruments: List<Instrument>)
    suspend fun deleteInstrument(instrument: Instrument)
    suspend fun deleteAllInstruments()

    // Single entity
    fun getInstrumentById(id: Int): Flow<Instrument?>

    // Search
    fun getInstrumentByName(query: String): Flow<List<Instrument>>
    suspend fun getCount(): Int

    // With video count – single filter
    fun getInstrumentsByArtistWithVideoCount(artistId: Int): Flow<List<Instrument>>
    fun getInstrumentsByTypeWithVideoCount(typeId: Int): Flow<List<Instrument>>
    fun getInstrumentsByDurationWithVideoCount(durationId: Int): Flow<List<Instrument>>

    // With video count – two filters
    fun getInstrumentsByArtistAndTypeWithVideoCount(artistId: Int, typeId: Int): Flow<List<Instrument>>
    fun getInstrumentsByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<Instrument>>
    fun getInstrumentsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<Instrument>>

    // With video count – three filters
    fun getInstrumentsByArtistAndTypeAndDurationWithVideoCount(
        artistId: Int,
        typeId: Int,
        durationId: Int
    ): Flow<List<Instrument>>

    // Flexible filters (returning Instrument)
    fun getInstrumentsByMultipleFilters(
        typeId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Instrument>>

    // Flexible filters (returning InstrumentWithVideoCount)
    fun getInstrumentsWithVideoCountByMultipleFilters(
        typeId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Instrument>>

    suspend fun getInstrumentCount(): Int
}