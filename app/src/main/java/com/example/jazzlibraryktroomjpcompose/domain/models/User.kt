package com.example.jazzlibraryktroomjpcompose.domain.models

/**
 * ✅ DOMAIN USER MODEL - NO Firebase dependency
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
) {
    val fullName: String
        get() = displayName ?: email ?: "User"
}

// User authentication state
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Loading(val message: String = "Authenticating...") : AuthState()
    data class Error(val exception: Exception) : AuthState()
}