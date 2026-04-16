package com.example.jazzlibraryktroomjpcompose.domain.models

data class AlbumContainsArtist(
    val artistId: Int,
    val albumId: Int,
    val isMain: Int
)

// ✅ BUSINESS LOGIC in domain class methods