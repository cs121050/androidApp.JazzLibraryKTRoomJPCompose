package com.example.jazzlibraryktroomjpcompose.data.remote.models


import com.google.gson.annotations.SerializedName

data class BootstrapResponse(
    @SerializedName("instrumentList")
    val instrumentList: List<RemoteInstrument>,

    @SerializedName("typeList")
    val typeList: List<RemoteType>,

    @SerializedName("durationList")
    val durationList: List<RemoteDuration>,

    @SerializedName("videoList")
    val videoList: List<RemoteVideo>,

    @SerializedName("artistList")
    val artistList: List<RemoteArtist>,

    @SerializedName("quoteList")
    val quoteList: List<RemoteQuote>,

    @SerializedName("videoContainsArtistList")
    val videoContainsArtistList: List<RemoteVideoContainsArtist>,

    @SerializedName("songList")
    val songList: List<RemoteSong>,

    @SerializedName("albumList")
    val albumList: List<RemoteAlbum>,

    @SerializedName("albumContainsArtistList")
    val albumContainsArtistList: List<RemoteAlbumContainsArtist>
)

data class RemoteInstrument(
    @SerializedName("instrument_id")
    val id: Int,

    @SerializedName("instrument_name")
    val name: String
)

data class RemoteType(
    @SerializedName("type_id")
    val id: Int,

    @SerializedName("type_name")
    val name: String
)

data class RemoteDuration(
    @SerializedName("duration_id")
    val id: Int,

    @SerializedName("duration_name")
    val name: String,

    @SerializedName("duration_description")
    val description: String
)

data class RemoteVideo(
    @SerializedName("video_id")
    val id: Int,

    @SerializedName("video_name")
    val name: String,

    @SerializedName("video_path")
    val path: String,

    @SerializedName("video_duration")
    val duration: String,

    @SerializedName("location_id")
    val locationId: String,

    @SerializedName("video_availability")
    val availability: String,

    @SerializedName("duration_id")
    val durationId: Int,

    @SerializedName("type_id")
    val typeId: Int
)

data class RemoteArtist(
    @SerializedName("artist_id")
    val id: Int,

    @SerializedName("artist_name")
    val name: String,

    @SerializedName("artist_surname")
    val surname: String,

    @SerializedName("artist_rank")
    val rank: Int?,

    @SerializedName("instrument_id")
    val instrumentId: Int,

    @SerializedName("spotify_playlist_id")
    val spotifyPlaylistId: String?,

    @SerializedName("musicbrainz_uuid")
    val musicbrainzUUID: String?,

    @SerializedName("discogs_id")
    val discogsId: Int?,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?,

    @SerializedName("wikipedia_data")
    val wikipedia_data: String?
)

data class RemoteQuote(
    @SerializedName("quote_id")
    val id: Int,

    @SerializedName("quote_text")
    val text: String,

    @SerializedName("artist_id")
    val artistId: Int?,

    @SerializedName("video_id")
    val videoId: Int?
)

data class RemoteVideoContainsArtist(
    @SerializedName("artist_id")
    val artistId: Int,

    @SerializedName("video_id")
    val videoId: Int
)

data class RemoteAlbumContainsArtist(
    @SerializedName("artist_id")
    val artistId: Int,

    @SerializedName("album_id")
    val albumId: Int,

    @SerializedName("is_main")
    val isMain: Int
)

data class RemoteSong(
    @SerializedName("song_id")
    val songId: Int,

    @SerializedName("main_artist_id")
    val mainArtistId: Int,

    @SerializedName("related_artists")
    val relatedArtists: String?,

    @SerializedName("album_id")
    val albumId: Int?,

    @SerializedName("song_title")
    val songTitle: String,

    @SerializedName("duration")
    val duration: String?,

    @SerializedName("yt_videoid")
    val ytVideoId: String?,

    @SerializedName("video_availability")
    val videoAvailability: String?
)

data class RemoteAlbum(
    @SerializedName("album_id")
    val albumId: Int,

    @SerializedName("youtube_video_id_for_thumbnail")
    val youtubeVideoIdForThumbnail: String?,

    @SerializedName("rating_average")
    val ratingAverage: Double?,

    @SerializedName("rating_count")
    val ratingCount: Int?,

    @SerializedName("year")
    val year: Int?,

    @SerializedName("released")
    val released: String?,

    @SerializedName("release_type")
    val releaseType: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?,

    @SerializedName("coverartarchive_thumb")
    val coverartarchiveThumb: String?,

    @SerializedName("extra_artists")
    val extraArtists: String?,

    @SerializedName("genres")
    val genres: String?,

    @SerializedName("labels")
    val labels: String?,

    @SerializedName("styles")
    val styles: String?,

    @SerializedName("tracklist")
    val tracklist: String?,

    @SerializedName("wikipedia_data")
    val wikipediaData: String?
)

