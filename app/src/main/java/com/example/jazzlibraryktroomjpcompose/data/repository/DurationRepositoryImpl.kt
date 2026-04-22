// data/repository/impl/DurationRepositoryImpl.kt
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

    override fun getAllDurationsWithCount(): Flow<List<Duration>> =
        database.durationDao().getAllDurationsWithCount()
            .map { entities -> entities.map { DurationMapper.toDomainWithCount(it) } }
}