package com.example.jazzlibraryktroomjpcompose.domain.usecases

import com.example.jazzlibraryktroomjpcompose.domain.models.AuthState
import com.example.jazzlibraryktroomjpcompose.domain.models.User
import com.example.jazzlibraryktroomjpcompose.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ DOMAIN LAYER - Pure business logic for authentication
 * - NO Firebase dependency
 * - Reusable everywhere
 * - Easy to test
 */
@Singleton
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    fun getAuthState(): Flow<AuthState> = authRepository.getAuthState()

    suspend fun getCurrentUser(): User? = authRepository.getCurrentUser()

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        // Add validation
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        return authRepository.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        // Add validation
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        return authRepository.signUpWithEmail(email, password)
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("ID token cannot be empty"))
        }
        return authRepository.signInWithGoogle(idToken)
    }

    suspend fun signInAnonymously(): Result<User> {
        return authRepository.signInAnonymously()
    }

    suspend fun signOut(): Result<Unit> {
        return authRepository.signOut()
    }

    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}