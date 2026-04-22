package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeWithVideoCount
import com.example.jazzlibraryktroomjpcompose.domain.models.Type
import kotlinx.coroutines.flow.Flow

interface  TypeRepository {
    fun getAllTypesWithCount(): Flow<List<Type>>
}