package com.example.jazzlibraryktroomjpcompose.domain.usecases.ArtistUseCase

import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import javax.inject.Inject

class RefreshArtistsUseCase @Inject constructor(
    private val repository: ArtistRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshArtists()
    }
}