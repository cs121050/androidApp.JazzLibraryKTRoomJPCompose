package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.Instrument
import kotlinx.coroutines.flow.Flow

interface InstrumentRepository {
    fun getAllInstrumentsWithArtistCount(): Flow<List<Instrument>>
    suspend fun getInstrumentCount(): Int
}