package com.seance.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.repository.AuthRepository
import com.seance.tv.data.repository.PlexRepository
import com.seance.tv.di.ServerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val rows: List<HomeRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val focusedItem: MediaItem? = null,
    val serverUrl: String = "",
    val authToken: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val serverManager: ServerManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val serverUrl = serverManager.serverUrl.first() ?: ""
            val token = authRepository.authToken.first() ?: ""
            _uiState.update { it.copy(serverUrl = serverUrl, authToken = token) }
            loadHome()
        }
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { plexRepository.buildHome() }
                .onSuccess { rows ->
                    _uiState.update { it.copy(rows = rows, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun shuffle() = loadHome()

    fun onItemFocused(item: MediaItem?) {
        _uiState.update { it.copy(focusedItem = item) }
    }

    fun imageUrl(path: String?): String? {
        val state = _uiState.value
        return plexRepository.buildImageUrl(path, state.serverUrl, state.authToken)
    }

    fun streamUrl(partKey: String): String {
        val state = _uiState.value
        return plexRepository.buildStreamUrl(partKey, state.serverUrl, state.authToken)
    }
}
