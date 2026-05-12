package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.AuthState
import com.example.jazzlibraryktroomjpcompose.domain.models.User
import kotlinx.coroutines.flow.Flow

/**
 * ✅ DOMAIN LAYER - Auth repository interface
 * Pure abstraction, NO Firebase dependency
 */
interface AuthRepository {

    /**
     * Get current auth state as a reactive Flow
     */
    fun getAuthState(): Flow<AuthState>

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User?

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Create new email account
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<User>

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<User>

    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean
}