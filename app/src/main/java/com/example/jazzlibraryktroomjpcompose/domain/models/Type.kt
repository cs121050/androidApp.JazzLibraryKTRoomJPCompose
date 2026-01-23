package com.example.jazzlibraryktroomjpcompose.domain.models

data class Type(
    val id: Int,
    val name: String
    // Note: We don't include nested objects here for simplicity
    // They'll be handled separately
) {
//easy bussines logic
//    val fullName: String
//        get() = "$name $surname"
}