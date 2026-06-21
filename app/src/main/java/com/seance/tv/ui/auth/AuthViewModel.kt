package com.seance.tv.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.api.PlexResourcesApi
import com.seance.tv.data.repository.AuthRepository
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
    private val resourcesApi: PlexResourcesApi,
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
                val serverUrl = serverManager.serverUrl.first()
                if (serverUrl != null) {
                    _authState.update { it.copy(isAuthenticated = true, isLoading = false) }
                } else {
                    // Token présent mais serveur pas encore découvert — on découvre
                    discoverServer()
                }
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
                    it.copy(pinCode = pin.code.uppercase(), pinId = pin.id, isLoading = false)
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
            discoverServer()
        } else {
            _authState.update { it.copy(error = "Délai dépassé. Réessayez.") }
        }
    }

    private suspend fun discoverServer() {
        _authState.update { it.copy(isLoading = true) }
        runCatching {
            val devices = resourcesApi.getResources()
            val server = devices.firstOrNull { it.isServer }
                ?: error("Aucun serveur Plex trouvé")
            val url = serverManager.selectBestServerUrl(server)
                ?: error("Aucune connexion disponible pour ce serveur")
            serverManager.saveServerUrl(url)
            _authState.update { it.copy(isAuthenticated = true, isLoading = false) }
        }.onFailure { e ->
            _authState.update { it.copy(isLoading = false, error = "Serveur introuvable : ${e.message}") }
        }
    }
}
