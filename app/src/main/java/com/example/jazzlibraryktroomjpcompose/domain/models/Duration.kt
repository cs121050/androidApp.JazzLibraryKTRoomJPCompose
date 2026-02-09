package com.example.jazzlibraryktroomjpcompose.domain.models

data class Duration(
    val id: Int,
    val name: String,
    val description: String,
    val videoCount: Int = 0 // Add this
)
// ✅ BUSINESS LOGIC in domain class methods