package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import kotlinx.coroutines.flow.Flow

interface InstrumentRepository {

    // Local operations
    fun getAllInstruments(): Flow<List<Instrument>>
    fun getInstrumentById(id: Int): Flow<Instrument?>
    fun searchInstruments(query: String): Flow<List<Instrument>>

    suspend fun saveInstrument(instrument: Instrument)
    suspend fun deleteInstrument(instrument: Instrument)

    // Remote operations
    suspend fun refreshInstruments(): Result<Unit>
    suspend fun syncInstrument(instrument: Int): Result<Instrument>

    // Combined operations
    suspend fun fetchAndCacheInstruments(): Result<List<Instrument>>
}