// domain/models/FilterHistoryEntry.kt
package com.example.jazzlibraryktroomjpcompose.domain.models

data class FilterHistoryEntry(
    val filterPathId: Int,
    val serialNumber: String,
    val timestamp: Long,
    val videoId: Int?,
    val videoName: String?,
    val videoPath: String?,
    val locationId: String?,
    val typeOfMedia: Int?
)