package com.example.jazzlibraryktroomjpcompose.domain.models

data class Instrument(
    val id: Int,
    val name: String,
    val videoCount: Int = 0 // Add this
)
// ✅ BUSINESS LOGIC in domain class methods