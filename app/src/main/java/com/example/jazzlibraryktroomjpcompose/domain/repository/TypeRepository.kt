package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Type
import kotlinx.coroutines.flow.Flow

interface TypeRepository {
    // Basic
    fun getAllTypes(): Flow<List<Type>>
    fun getAllTypesWithCount(): Flow<List<Type>>
    suspend fun insertType(type: Type)
    suspend fun insertAllTypes(types: List<Type>)
    suspend fun updateType(type: Type)
    suspend fun deleteType(type: Type)
    suspend fun deleteAllTypes()

    // Single entity
    fun getTypeById(id: Int): Flow<Type?>

    // Search
    fun searchTypes(query: String): Flow<List<Type>>

    // With video count – single filter
    fun getTypesByDurationWithVideoCount(durationId: Int): Flow<List<Type>>
    fun getTypesByArtistWithVideoCount(artistId: Int): Flow<List<Type>>
    fun getTypesByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Type>>

    // With video count – two filters
    fun getTypesByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<Type>>
    fun getTypesByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<Type>>
    fun getTypesByInstrumentAndArtistWithVideoCount(instrumentId: Int, artistId: Int): Flow<List<Type>>

    // With video count – three filters
    fun getTypesByInstrumentAndArtistAndDurationWithVideoCount(
        instrumentId: Int,
        artistId: Int,
        durationId: Int
    ): Flow<List<Type>>

    // Flexible filters (returning Type)
    fun getTypesByMultipleFilters(
        instrumentId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Type>>

    // Flexible filters (returning TypeWithVideoCount)
    fun getTypesWithVideoCountByMultipleFilters(
        instrumentId: Int = 0,
        durationId: Int = 0,
        artistId: Int = 0
    ): Flow<List<Type>>
}