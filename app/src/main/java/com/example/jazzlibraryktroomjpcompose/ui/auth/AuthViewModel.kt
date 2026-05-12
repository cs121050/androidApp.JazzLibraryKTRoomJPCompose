package com.example.jazzlibraryktroomjpcompose.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.domain.models.AuthState
import com.example.jazzlibraryktroomjpcompose.domain.models.User
import com.example.jazzlibraryktroomjpcompose.domain.usecases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ✅ PRESENTATION LAYER - Auth ViewModel
 * Handles all authentication UI state
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authUseCase.getAuthState().collect { state ->
                _authState.value = state
            }
        }
    }

    fun signInWithEmail() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Please enter email and password"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authUseCase.signInWithEmail(_email.value, _password.value)

            result.onSuccess { user ->
                _loading.value = false
                clearForm()
            }
            result.onFailure { exception ->
                _loading.value = false
                _error.value = exception.message ?: "Sign-in failed"
            }
        }
    }

    fun signUpWithEmail() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Please enter email and password"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authUseCase.signUpWithEmail(_email.value, _password.value)

            result.onSuccess { user ->
                _loading.value = false
                clearForm()
            }
            result.onFailure { exception ->
                _loading.value = false
                _error.value = exception.message ?: "Sign-up failed"
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authUseCase.signInWithGoogle(idToken)

            result.onSuccess { user ->
                _loading.value = false
                clearForm()
            }
            result.onFailure { exception ->
                _loading.value = false
                _error.value = exception.message ?: "Google sign-in failed"
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authUseCase.signInAnonymously()

            result.onSuccess { user ->
                _loading.value = false
                clearForm()
            }
            result.onFailure { exception ->
                _loading.value = false
                _error.value = exception.message ?: "Anonymous sign-in failed"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val result = authUseCase.signOut()
            result.onFailure { exception ->
                _error.value = exception.message ?: "Sign-out failed"
            }
        }
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _error.value = null
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private fun clearForm() {
        _email.value = ""
        _password.value = ""
    }
}