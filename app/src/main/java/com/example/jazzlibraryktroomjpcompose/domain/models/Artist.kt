package com.example.jazzlibraryktroomjpcompose.domain.models

data class Artist(
    val id: Int,
    val name: String,
    val surname: String,
    val instrumentId: Int,
    val rank: Int? = 0,
    val videoCount: Int = 0,
    val spotifyPlaylistId: String?,
    val musicbrainzUUID: String?,
    val discogsId: Int? = 0
    // Note: We don't include nested objects here for simplicity
    // They'll be handled separately
) {
    // ✅ BUSINESS LOGIC in domain class methods

    val fullName: String
        get() = "$name $surname"

    val fullMusicBrainzURL: String?
        get() = musicbrainzUUID?.let { "https://musicbrainz.org/artist/$it" }

    val fullSpotifyPlaylistURL: String?
        get() = spotifyPlaylistId?.let { "https://open.spotify.com/playlist/$it" }

    val fullDiscogsURL: String?
        get() = discogsId?.let { "https://www.discogs.com/artist/$it" }
}
