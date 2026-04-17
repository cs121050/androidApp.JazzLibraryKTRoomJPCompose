package com.example.jazzlibraryktroomjpcompose.domain.models

import com.example.jazzlibraryktroomjpcompose.domain.models.utils.YouTubeUtils

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
    val extraArtists: String?,   // JSON array
    val genres: String?,         // JSON array
    val labels: String?,         // JSON array
    val styles: String?,         // JSON array
    val tracklist: String?,      // JSON array
    val wikipediaData: String?   // JSON array
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