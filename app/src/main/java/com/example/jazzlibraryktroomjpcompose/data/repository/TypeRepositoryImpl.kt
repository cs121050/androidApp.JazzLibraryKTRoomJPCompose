// data/repository/impl/TypeRepositoryImpl.kt
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

    override fun getAllTypesWithCount(): Flow<List<Type>> =
        database.typeDao().getAllTypesWithCount()
            .map { entities -> entities.map { TypeMapper.toDomainWithCount(it) } }
}