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
    val discogsId: Int? = 0,
    val wikipediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val imageAuthor: String? = null,
    val imageLicense: String? = null,
    val imageSourceUrl: String? = null,
    val wikipediaData: String? = null
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
