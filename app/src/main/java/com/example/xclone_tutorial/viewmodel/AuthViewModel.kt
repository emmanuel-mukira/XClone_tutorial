package com.example.xclone_tutorial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xclone_tutorial.firebase.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication screens (Login and SignUp)
 * Handles all authentication logic and state management
 */
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // UI State for authentication
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Current user
    val currentUser: FirebaseUser?
        get() = authRepository.getCurrentUser()

    /**
     * Sign up a new user
     */
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val cleanName = name.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanPassword = password.trim()
            authRepository.signUp(
                name = cleanName,
                email = cleanEmail,
                password = cleanPassword,
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onError = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            )
        }
    }

    /**
     * Log in existing user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val cleanEmail = email.trim().lowercase()
            val cleanPassword = password.trim()
            authRepository.login(
                email = cleanEmail,
                password = cleanPassword,
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onError = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    /**
     * Log out current user
     */
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }

    /**
     * Reset auth state to idle (e.g., after navigation)
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

/**
 * Sealed class representing authentication states
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
