// file: domain/repository/FilterPathContainsMediaRepository.kt
package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPathContainsMedia

interface FilterPathContainsMediaRepository {

    /** Insert a single association */
    suspend fun insert(entry: FilterPathContainsMedia)

    /** Get video ID associated with a given filter path ID */
    suspend fun getVideoIdForFilterPath(filterPathId: Int): Int?

    /** Delete all associations whose filter path has timestamp > given time */
    suspend fun deleteAllNewerThan(timestamp: Long)

    /** Delete all associations */
    suspend fun deleteAll()
}