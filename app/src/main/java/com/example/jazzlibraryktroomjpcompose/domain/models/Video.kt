package com.example.jazzlibraryktroomjpcompose.domain.models

import com.example.jazzlibraryktroomjpcompose.domain.models.utils.YouTubeUtils

data class Video(
    val id: Int,
    val name: String,
    val duration: String,
    val path: String,
    val locationId: String,
    val availability: String,
    val durationId: Int,
    val typeId: Int
) {
    // ✅ BUSINESS LOGIC in domain class methods
    /**
     * Returns the YouTube thumbnail URL for this video.
     * @param quality Optional quality string (default: "hqdefault").
     */
    fun getThumbnailUrl(quality: String = "hqdefault"): String? {
        val videoId = YouTubeUtils.extractVideoId(path)
        return videoId?.let { YouTubeUtils.buildThumbnailUrl(it, quality) }
    }
}
