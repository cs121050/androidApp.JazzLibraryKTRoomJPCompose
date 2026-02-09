package com.example.jazzlibraryktroomjpcompose.domain.models

data class Artist(
    val id: Int,
    val name: String,
    val surname: String,
    val instrumentId: Int,
    val rank: Int? = 0,
    val videoCount: Int = 0 // Add this
    // Note: We don't include nested objects here for simplicity
    // They'll be handled separately
) {
    // ✅ BUSINESS LOGIC in domain class methods

    val fullName: String
    get() = "$name $surname"
}
