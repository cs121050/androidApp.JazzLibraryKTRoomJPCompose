package com.example.jazzlibraryktroomjpcompose.data.repository.impl

import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.mappers.InstrumentMapper
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import com.example.jazzlibraryktroomjpcompose.domain.repository.InstrumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstrumentRepositoryImpl @Inject constructor(
    private val database: JazzDatabase
) : InstrumentRepository {

    override fun getAllInstruments(): Flow<List<Instrument>> =
        database.instrumentDao().getAllInstruments().map { entities -> entities.map { InstrumentMapper.toDomain(it) } }

    override fun getAllInstrumentsWithArtistCount(): Flow<List<Instrument>> =
        database.instrumentDao().getAllInstrumentsWithArtistCount()
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override suspend fun insertInstrument(instrument: Instrument) {
        database.instrumentDao().insertInstrument(InstrumentMapper.toEntity(instrument))
    }

    override suspend fun insertAllInstruments(instruments: List<Instrument>) {
        database.instrumentDao().insertAllInstruments(instruments.map { InstrumentMapper.toEntity(it) })
    }

    override suspend fun deleteInstrument(instrument: Instrument) {
        database.instrumentDao().deleteInstrument(InstrumentMapper.toEntity(instrument))
    }

    override suspend fun deleteAllInstruments() {
        database.instrumentDao().deleteAllInstruments()
    }

    override fun getInstrumentById(id: Int): Flow<Instrument?> =
        database.instrumentDao().getInstrumentById(id).map { it?.let { InstrumentMapper.toDomain(it) } }

    override fun getInstrumentByName(query: String): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentByName(query).map { entities -> entities.map { InstrumentMapper.toDomain(it) } }

    override suspend fun getCount(): Int = database.instrumentDao().getCount()

    override fun getInstrumentsByArtistWithVideoCount(artistId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByArtistWithVideoCount(artistId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByTypeWithVideoCount(typeId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByTypeWithVideoCount(typeId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByDurationWithVideoCount(durationId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByDurationWithVideoCount(durationId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByArtistAndTypeWithVideoCount(artistId: Int, typeId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByArtistAndTypeWithVideoCount(artistId, typeId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByArtistAndDurationWithVideoCount(artistId: Int, durationId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByArtistAndDurationWithVideoCount(artistId, durationId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByTypeAndDurationWithVideoCount(typeId: Int, durationId: Int): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByTypeAndDurationWithVideoCount(typeId, durationId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByArtistAndTypeAndDurationWithVideoCount(
        artistId: Int, typeId: Int, durationId: Int
    ): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByArtistAndTypeAndDurationWithVideoCount(artistId, typeId, durationId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override fun getInstrumentsByMultipleFilters(
        typeId: Int, durationId: Int, artistId: Int
    ): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsByMultipleFilters(typeId, durationId, artistId)
            .map { entities -> entities.map { InstrumentMapper.toDomain(it) } }

    override fun getInstrumentsWithVideoCountByMultipleFilters(
        typeId: Int, durationId: Int, artistId: Int
    ): Flow<List<Instrument>> =
        database.instrumentDao().getInstrumentsWithVideoCountByMultipleFilters(typeId, durationId, artistId)
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override suspend fun getInstrumentCount(): Int = database.instrumentDao().getInstrumentCount()
}