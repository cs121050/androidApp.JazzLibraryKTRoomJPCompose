package com.example.jazzlibraryktroomjpcompose.domain.models

data class Video(
    val id: Int,
    val name: String,
    val duration: String,
    val path: String,
    val locationId: String,
    val availability: String,
    val durationId: Int,
    val typeId: Int
)