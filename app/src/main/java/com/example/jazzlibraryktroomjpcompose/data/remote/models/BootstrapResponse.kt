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
    val videoContainsArtistList: List<RemoteVideoContainsArtist>
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
    val instrumentId: Int
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