package com.example.jazzlibraryktroomjpcompose.domain.models

data class Quote(
    val id: Int,
    val text: String,
    val artistId: Int?,
    val videoId: Int? = null // Added videoId
)