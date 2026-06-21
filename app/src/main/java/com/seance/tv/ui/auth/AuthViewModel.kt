package com.seance.tv.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.api.PlexResourcesApi
import com.seance.tv.data.model.HomeUser
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

enum class AuthPhase { Loading, Pin, Profiles, Ready }

data class AuthState(
    val phase: AuthPhase = AuthPhase.Loading,
    val isLoading: Boolean = true,
    val pinCode: String = "",
    val pinId: Long = 0L,
    val profiles: List<HomeUser> = emptyList(),
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
            authRepository.ensureAdminToken()
            val token = authRepository.authToken.first()
            if (!token.isNullOrBlank()) {
                proceedAfterAuth()
            } else {
                _authState.update { it.copy(phase = AuthPhase.Pin, isLoading = false) }
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
            proceedAfterAuth()
        } else {
            _authState.update { it.copy(error = "Délai dépassé. Réessayez.") }
        }
    }

    /** Après obtention d'un token : afficher les profils si plusieurs, sinon entrer. */
    private suspend fun proceedAfterAuth() {
        _authState.update { it.copy(phase = AuthPhase.Loading, isLoading = true, error = null) }
        val users = authRepository.getHomeUsers()
        if (users.size > 1) {
            _authState.update {
                it.copy(phase = AuthPhase.Profiles, profiles = users, isLoading = false)
            }
        } else {
            ensureServerThenReady()
        }
    }

    /** Sélection d'un profil depuis l'écran « Qui regarde ? ». */
    fun selectProfile(user: HomeUser, pin: String? = null) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                if (user.admin && !user.requiresPin) {
                    authRepository.useAdminAsActive()
                } else {
                    authRepository.switchProfile(user.uuid, pin)
                        ?: error("Bascule impossible")
                }
                // Re-découverte forcée : récupère le token d'accès serveur propre au
                // profil (sinon les utilisateurs gérés reçoivent un 401 sur le PMS).
                discoverServer()
            }.onFailure { e ->
                _authState.update {
                    it.copy(isLoading = false, error = "Profil indisponible : ${e.message}")
                }
            }
        }
    }

    /** S'assure qu'un serveur est connu puis passe en phase Ready. */
    private suspend fun ensureServerThenReady() {
        val serverUrl = serverManager.serverUrl.first()
        if (serverUrl != null) {
            _authState.update { it.copy(phase = AuthPhase.Ready, isLoading = false) }
        } else {
            discoverServer()
        }
    }

    private suspend fun discoverServer() {
        _authState.update { it.copy(isLoading = true) }
        runCatching {
            val token = authRepository.authToken.first() ?: error("Token manquant")
            val clientId = authRepository.getOrCreateClientId()
            val devices = resourcesApi.getResources(clientId = clientId, token = token)
            val server = devices.firstOrNull { it.isServer }
                ?: error("Aucun serveur Plex trouvé")
            val url = serverManager.selectBestServerUrl(server)
                ?: error("Aucune connexion disponible pour ce serveur")
            serverManager.saveServerUrl(url)
            // Token d'accès propre au serveur pour ce profil (essentiel pour les
            // utilisateurs gérés ; pour le propriétaire il vaut son token de compte).
            server.accessToken?.takeIf { it.isNotBlank() }?.let { authRepository.setActiveToken(it) }
            _authState.update { it.copy(phase = AuthPhase.Ready, isLoading = false) }
        }.onFailure { e ->
            _authState.update { it.copy(isLoading = false, error = "Serveur introuvable : ${e.message}") }
        }
    }
}
