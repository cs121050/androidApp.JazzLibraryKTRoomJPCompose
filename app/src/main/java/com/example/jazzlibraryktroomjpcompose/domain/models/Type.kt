package com.example.jazzlibraryktroomjpcompose.domain.models

data class Type(
    val id: Int,
    val name: String,
    val videoCount: Int = 0 // Add this
    // Note: We don't include nested objects here for simplicity
    // They'll be handled separately
) {
    // ✅ BUSINESS LOGIC in domain class methods
}