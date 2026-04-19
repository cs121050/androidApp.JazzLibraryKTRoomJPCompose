package com.example.jazzlibraryktroomjpcompose.domain.models

import com.example.jazzlibraryktroomjpcompose.domain.models.utils.YouTubeUtils
// domain/models/Album.kt
data class Album(
    val albumId: Int,
    val youtubeVideoIdForThumbnail: String?,
    val ratingAverage: Double?,
    val ratingCount: Int?,
    val released: String?,
    val releaseType: String?,
    val title: String,
    val wikipediaUrl: String?,
    val coverartarchiveThumb: String?,
    val extraArtists: String?,   // JSON
    val genres: String?,         // JSON
    val labels: String?,         // JSON
    val styles: String?,         // JSON
    val tracklist: String?,      // JSON
    val wikipediaData: String?,  // JSON

    val isMain: Int = 0,          // ← NEW: 1 = main album for an artist, 0 = not main
        val artistId: Int? = null,
        val artistFullName: String? = null,
        val artistInstrumentId: Int? = null


) {
    /**
     * Returns the YouTube thumbnail URL for this album,
     * using the stored [youtubeVideoIdForThumbnail] if available.
     * @param quality Optional quality string (default: "hqdefault").
     */
    fun getThumbnailUrl(quality: String = "hqdefault"): String? {
        return youtubeVideoIdForThumbnail?.let { videoId ->
            YouTubeUtils.buildThumbnailUrl(videoId, quality)
        }
    }
}