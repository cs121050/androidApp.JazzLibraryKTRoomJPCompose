// data/repository/impl/InstrumentRepositoryImpl.kt
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

    override fun getAllInstrumentsWithArtistCount(): Flow<List<Instrument>> =
        database.instrumentDao().getAllInstrumentsWithArtistCount()
            .map { entities -> entities.map { InstrumentMapper.toDomainWithCount(it) } }

    override suspend fun getInstrumentCount(): Int =
        database.instrumentDao().getInstrumentCount()
}