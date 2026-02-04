package com.example.jazzlibraryktroomjpcompose.data.mappers


import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*
import com.example.jazzlibraryktroomjpcompose.data.remote.models.*

object RemoteToEntityMappers {

    fun RemoteInstrument.toInstrumentEntity(): InstrumentRoomEntity {
        return InstrumentRoomEntity(
            id = this.id,
            name = this.name
        )
    }

    fun RemoteType.toTypeEntity(): TypeRoomEntity {
        return TypeRoomEntity(
            id = this.id,
            name = this.name
        )
    }

    fun RemoteDuration.toDurationEntity(): DurationRoomEntity {
        return DurationRoomEntity(
            id = this.id,
            name = this.name,
            description = this.description
        )
    }

    fun RemoteVideo.toVideoEntity(): VideoRoomEntity {
        return VideoRoomEntity(
            id = this.id,
            name = this.name,
            duration = this.duration, // Note: You need to extract this from somewhere or calculate it
            path = this.path,
            locationId = this.locationId,
            availability = this.availability,
            durationId = this.durationId,
            typeId = this.typeId
        )
    }

    fun RemoteArtist.toArtistEntity(): ArtistRoomEntity {
        return ArtistRoomEntity(
            id = this.id,
            name = this.name,
            surname = this.surname,
            instrumentId = this.instrumentId,
            rank = this.rank ?: 0
        )
    }

    fun RemoteQuote.toQuoteEntity(): QuoteRoomEntity {
        return QuoteRoomEntity(
            id = this.id,
            text = this.text,
            artistId = this.artistId,
            videoId = this.videoId
        )
    }

    fun RemoteVideoContainsArtist.toVideoContainsArtistEntity(): VideoContainsArtistRoomEntity {
        return VideoContainsArtistRoomEntity(
            artistId = this.artistId,
            videoId = this.videoId
        )
    }

    // Extension functions for lists with clearer names
    fun List<RemoteInstrument>.toInstrumentEntities(): List<InstrumentRoomEntity> {
        return this.map { it.toInstrumentEntity() }
    }

    fun List<RemoteType>.toTypeEntities(): List<TypeRoomEntity> {
        return this.map { it.toTypeEntity() }
    }

    fun List<RemoteDuration>.toDurationEntities(): List<DurationRoomEntity> {
        return this.map { it.toDurationEntity() }
    }

    fun List<RemoteVideo>.toVideoEntities(): List<VideoRoomEntity> {
        return this.map { it.toVideoEntity() }
    }

    fun List<RemoteArtist>.toArtistEntities(): List<ArtistRoomEntity> {
        return this.map { it.toArtistEntity() }
    }

    fun List<RemoteQuote>.toQuoteEntities(): List<QuoteRoomEntity> {
        return this.map { it.toQuoteEntity() }
    }

    fun List<RemoteVideoContainsArtist>.toVideoContainsArtistEntities(): List<VideoContainsArtistRoomEntity> {
        return this.map { it.toVideoContainsArtistEntity() }
    }
}