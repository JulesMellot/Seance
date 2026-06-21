package com.seance.tv.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.repository.AuthRepository
import com.seance.tv.data.repository.PlexRepository
import com.seance.tv.di.ServerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val pinCode: String = "",
    val pinId: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val serverManager: ServerManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        viewModelScope.launch {
            val token = authRepository.authToken.first()
            if (!token.isNullOrBlank()) {
                _authState.update { it.copy(isAuthenticated = true, isLoading = false) }
            } else {
                _authState.update { it.copy(isLoading = false) }
                startPinAuth()
            }
        }
    }

    fun startPinAuth() {
        viewModelScope.launch {
            runCatching {
                _authState.update { it.copy(isLoading = true, error = null) }
                val pin = authRepository.createPin()
                _authState.update {
                    it.copy(
                        pinCode = pin.code.uppercase(),
                        pinId = pin.id,
                        isLoading = false
                    )
                }
                pollForAuth(pin.id)
            }.onFailure { e ->
                _authState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun pollForAuth(pinId: Long) {
        val token = authRepository.pollForToken(pinId)
        if (token != null) {
            _authState.update { it.copy(isAuthenticated = true) }
        } else {
            _authState.update { it.copy(error = "Délai d'authentification dépassé. Réessayez.") }
        }
    }
}
