package com.example.jazzlibraryktroomjpcompose.domain.models.utils


object YouTubeUtils {
    // Regex to extract the 11-character video ID from various YouTube URL formats
    private val videoIdRegex = "(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([a-zA-Z0-9_-]{11})".toRegex()

    /**
     * Extracts the YouTube video ID from a full video URL.
     * Returns null if the URL is invalid or doesn't match known patterns.
     */
    fun extractVideoId(url: String): String? {
        return videoIdRegex.find(url)?.groupValues?.get(1)
    }

    /**
     * Builds a thumbnail URL for a given video ID and desired quality.
     * Quality options: default, mqdefault, hqdefault, sddefault, maxresdefault.
     */
    fun buildThumbnailUrl(videoId: String, quality: String = "hqdefault"): String {
        return "https://img.youtube.com/vi/$videoId/$quality.jpg"
    }
}