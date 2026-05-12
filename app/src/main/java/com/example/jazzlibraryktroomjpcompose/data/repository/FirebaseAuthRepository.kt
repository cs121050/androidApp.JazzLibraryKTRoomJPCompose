package com.example.jazzlibraryktroomjpcompose.data.repository

import android.util.Log
import com.example.jazzlibraryktroomjpcompose.domain.models.AuthState
import com.example.jazzlibraryktroomjpcompose.domain.models.User
import com.example.jazzlibraryktroomjpcompose.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

/**
 * ✅ DATA LAYER - Firebase implementation of AuthRepository
 * Handles all Firebase Auth operations
 * Returns domain models, not Firebase models
 */
@Singleton
class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    /**
     * Listen to auth state changes and emit domain models
     */
    override fun getAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val user = firebaseUser.toDomainUser()
                trySend(AuthState.Authenticated(user))
                Log.d(TAG, "User authenticated: ${user.email}")
            } else {
                trySend(AuthState.Unauthenticated)
                Log.d(TAG, "User unauthenticated")
            }
        }

        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun getCurrentUser(): User? {
        return auth.currentUser?.toDomainUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toDomainUser()
            if (user != null) {
                Log.d(TAG, "Email sign-in successful: $email")
                Result.success(user)
            } else {
                Result.failure(Exception("User not found after sign-in"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.toDomainUser()
            if (user != null) {
                Log.d(TAG, "Email sign-up successful: $email")
                Result.success(user)
            } else {
                Result.failure(Exception("User not found after sign-up"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.toDomainUser()
            if (user != null) {
                Log.d(TAG, "Google sign-in successful: ${user.email}")
                Result.success(user)
            } else {
                Result.failure(Exception("User not found after Google sign-in"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<User> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user?.toDomainUser()
            if (user != null) {
                Log.d(TAG, "Anonymous sign-in successful")
                Result.success(user)
            } else {
                Result.failure(Exception("Anonymous sign-in failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out failed", e)
            Result.failure(e)
        }
    }

    override fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Helper: Convert Firebase user to domain user
     */
    private fun com.google.firebase.auth.FirebaseUser.toDomainUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous
        )
    }
}