package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.DurationMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Duration
import com.example.jazzlibraryktroomjpcompose.domain.repository.DurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DurationRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : DurationRepository {

    override fun getAllDurations(): Flow<List<Duration>> =
        database.durationDao().getAllDurations().map { entities -> entities.map { DurationMapper.toDomain(it) } }

    override fun getAllDurationsWithCount(): Flow<List<Duration>> =
        database.durationDao().getAllDurationsWithCount()
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    override suspend fun insertDuration(duration: Duration) {
        database.durationDao().insertDuration(DurationMapper.toEntity(duration))
    }

    override suspend fun insertAllDurations(durations: List<Duration>) {
        database.durationDao().insertAllDurations(durations.map { DurationMapper.toEntity(it) })
    }

    override suspend fun updateDuration(duration: Duration) {
        database.durationDao().updateDuration(DurationMapper.toEntity(duration))
    }

    override suspend fun deleteDuration(duration: Duration) {
        database.durationDao().deleteDuration(DurationMapper.toEntity(duration))
    }

    override suspend fun deleteAllDurations() {
        database.durationDao().deleteAllDurations()
    }

    override fun getDurationById(id: Int): Flow<Duration?> =
        database.durationDao().getDurationById(id).map { it?.let { DurationMapper.toDomain(it) } }

    override fun searchDurations(query: String): Flow<List<Duration>> =
        database.durationDao().searchDurations(query).map { entities -> entities.map { DurationMapper.toDomain(it) } }

    // With video count – single filter
    override fun getDurationsByTypeWithVideoCount(typeId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByTypeWithVideoCount(typeId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    override fun getDurationsByArtistWithVideoCount(artistId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByArtistWithVideoCount(artistId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    override fun getDurationsByInstrumentWithVideoCount(instrumentId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByInstrumentWithVideoCount(instrumentId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    // With video count – two filters
    override fun getDurationsByTypeAndArtistWithVideoCount(typeId: Int, artistId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByTypeAndArtistWithVideoCount(typeId, artistId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    override fun getDurationsByInstrumentAndTypeWithVideoCount(instrumentId: Int, typeId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByInstrumentAndTypeWithVideoCount(instrumentId, typeId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    override fun getDurationsByArtistAndInstrumentWithVideoCount(artistId: Int, instrumentId: Int): Flow<List<Duration>> =
        database.durationDao().getDurationsByArtistAndInstrumentWithVideoCount(artistId, instrumentId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    // With video count – three filters
    override fun getDurationsByInstrumentAndTypeAndArtistWithVideoCount(
        instrumentId: Int, typeId: Int, artistId: Int
    ): Flow<List<Duration>> =
        database.durationDao().getDurationsByInstrumentAndTypeAndArtistWithVideoCount(instrumentId, typeId, artistId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }

    // Flexible filters (returning Duration)
    override fun getDurationsByMultipleFilters(
        instrumentId: Int, typeId: Int, artistId: Int
    ): Flow<List<Duration>> =
        database.durationDao().getDurationsByMultipleFilters(instrumentId, typeId, artistId)
            .map { entities -> entities.map { DurationMapper.toDomain(it) } }

    // Flexible filters (returning DurationWithVideoCount)
    override fun getDurationsWithVideoCountByMultipleFilters(
        instrumentId: Int, typeId: Int, artistId: Int
    ): Flow<List<Duration>> =
        database.durationDao().getDurationsWithVideoCountByMultipleFilters(instrumentId, typeId, artistId)
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }
}