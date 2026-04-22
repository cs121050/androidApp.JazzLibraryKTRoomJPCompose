// file: domain/models/FilterPathContainsMedia.kt
package com.example.jazzlibraryktroomjpcompose.domain.models

/**
 * Represents the association between a saved filter path and a media item (video).
 * Used to remember which video was last selected for a given filter combination.
 */
data class FilterPathContainsMedia(
    val id: Int = 0,                     // auto-generated primary key
    val filterPathId: Int,               // foreign key to FilterPathRoomEntity
    val videoId: Int?,                   // associated video ID (nullable if typeOfMedia used)
    val typeOfMedia: Int?                // optional: could represent album, song, etc.
)