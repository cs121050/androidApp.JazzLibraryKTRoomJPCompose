package com.example.jazzlibraryktroomjpcompose.domain.usecases.ArtistUseCase

import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val repository: ArtistRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return repository.getAllArtists()
    }

    operator fun invoke(query: String): Flow<List<Artist>> {
        return repository.searchArtists(query)
    }

    operator fun invoke(instrumentId: Int): Flow<List<Artist>> {
        return repository.getArtistsByInstrument(instrumentId)
    }
}