package com.example.jazzlibraryktroomjpcompose.domain.models

import com.example.jazzlibraryktroomjpcompose.domain.models.utils.YouTubeUtils

data class Song(
    val songId: Int,
    val mainArtistId: Int,
    val relatedArtists: String?,   // comma-separated artist IDs or names
    val albumId: Int?,
    val songTitle: String?,
    val duration: String?,
    val ytVideoId: String?,
    val videoAvailability: String?
) {
    /**
     * Returns the YouTube thumbnail URL for this song.
     * @param quality Optional quality string (default: "hqdefault").
     */
    fun getThumbnailUrl(quality: String = "hqdefault"): String? {
        return ytVideoId?.let { videoId ->
            YouTubeUtils.buildThumbnailUrl(videoId, quality)
        }
    }
}