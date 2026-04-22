package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Artist
import com.example.jazzlibraryktroomjpcompose.domain.models.Duration
import kotlinx.coroutines.flow.Flow

interface DurationRepository {
    fun getAllDurationsWithCount(): Flow<List<Duration>>
}