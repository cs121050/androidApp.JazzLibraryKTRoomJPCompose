package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.TypeMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Type
import com.example.jazzlibraryktroomjpcompose.domain.repository.TypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TypeRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : TypeRepository {

    override fun getAllTypes(): Flow<List<Type>> =
        database.typeDao().getAllTypes().map { entities -> entities.map { TypeMapper.toDomain(it) } }

    override fun getAllTypesWithCount(): Flow<List<Type>> =
        database.typeDao().getAllTypesWithCount()
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    override suspend fun insertType(type: Type) {
        database.typeDao().insertType(TypeMapper.toEntity(type))
    }

    override suspend fun insertAllTypes(types: List<Type>) {
        database.typeDao().insertAllTypes(types.map { TypeMapper.toEntity(it) })
    }

    override suspend fun updateType(type: Type) {
        database.typeDao().updateType(TypeMapper.toEntity(type))
    }

    override suspend fun deleteType(type: Type) {
        database.typeDao().deleteType(TypeMapper.toEntity(type))
    }

    override suspend fun deleteAllTypes() {
        database.typeDao().deleteAllTypes()
    }

    override fun getTypeById(id: Int): Flow<Type?> =
        database.typeDao().getTypeById(id).map { it?.let { TypeMapper.toDomain(it) } }

    override fun searchTypes(query: String): Flow<List<Type>> =
        database.typeDao().searchTypes(query).map { entities -> entities.map { TypeMapper.toDomain(it) } }

    // With video count – single filter
    override fun getTypesByDurationWithVideoCount(durationId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByDurationWithVideoCount(durationId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    override fun getTypesByArtistWithVideoCount(artistId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByArtistWithVideoCount(artistId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    override fun getTypesByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByInstrumentWithVideoCount(instrumentId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    // With video count – two filters
    override fun getTypesByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByArtistAndDurationWithVideoCount(artistId, durationId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    override fun getTypesByInstrumentAndDurationWithVideoCount(instrumentId: Int, durationId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByInstrumentAndDurationWithVideoCount(instrumentId, durationId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    override fun getTypesByInstrumentAndArtistWithVideoCount(instrumentId: Int, artistId: Int): Flow<List<Type>> =
        database.typeDao().getTypesByInstrumentAndArtistWithVideoCount(instrumentId, artistId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    // With video count – three filters
    override fun getTypesByInstrumentAndArtistAndDurationWithVideoCount(
        instrumentId: Int, artistId: Int, durationId: Int
    ): Flow<List<Type>> =
        database.typeDao().getTypesByInstrumentAndArtistAndDurationWithVideoCount(instrumentId, artistId, durationId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }

    // Flexible filters (returning Type)
    override fun getTypesByMultipleFilters(
        instrumentId: Int, durationId: Int, artistId: Int
    ): Flow<List<Type>> =
        database.typeDao().getTypesByMultipleFilters(instrumentId, durationId, artistId)
            .map { entities -> entities.map { TypeMapper.toDomain(it) } }

    // Flexible filters (returning TypeWithVideoCount)
    override fun getTypesWithVideoCountByMultipleFilters(
        instrumentId: Int, durationId: Int, artistId: Int
    ): Flow<List<Type>> =
        database.typeDao().getTypesWithVideoCountByMultipleFilters(instrumentId, durationId, artistId)
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }
}