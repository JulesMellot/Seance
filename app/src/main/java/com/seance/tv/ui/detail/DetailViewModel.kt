package com.seance.tv.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.MediaItem
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

data class DetailUiState(
    val item: MediaItem? = null,
    val seasons: List<MediaItem> = emptyList(),
    val selectedSeason: MediaItem? = null,
    val episodes: List<MediaItem> = emptyList(),
    val similar: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val serverUrl: String = "",
    val token: String = ""
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val serverManager: ServerManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadedKey: String? = null

    fun load(ratingKey: String) {
        if (loadedKey == ratingKey) return
        loadedKey = ratingKey
        viewModelScope.launch {
            val serverUrl = serverManager.serverUrl.first() ?: ""
            val token = authRepository.authToken.first() ?: ""
            _uiState.update { it.copy(isLoading = true, error = null, serverUrl = serverUrl, token = token) }
            runCatching {
                val item = plexRepository.getMetadata(ratingKey) ?: error("Média introuvable")
                val similar = runCatching { plexRepository.getSimilar(item) }.getOrDefault(emptyList())
                if (item.isShow) {
                    val children = plexRepository.getChildren(ratingKey)
                    val seasons = children.filter { it.isSeason }
                    val firstSeason = seasons.firstOrNull()
                    val episodes = firstSeason?.let { plexRepository.getChildren(it.ratingKey) } ?: emptyList()
                    _uiState.update {
                        it.copy(
                            item = item,
                            seasons = seasons,
                            selectedSeason = firstSeason,
                            episodes = episodes,
                            similar = similar,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(item = item, similar = similar, isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectSeason(season: MediaItem) {
        if (season.ratingKey == _uiState.value.selectedSeason?.ratingKey) return
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSeason = season) }
            runCatching {
                val episodes = plexRepository.getChildren(season.ratingKey)
                _uiState.update { it.copy(episodes = episodes) }
            }
        }
    }

    fun imageUrl(path: String?): String? {
        val state = _uiState.value
        return plexRepository.buildImageUrl(path, state.serverUrl, state.token)
    }

    fun streamUrl(partKey: String): String {
        val state = _uiState.value
        return plexRepository.buildStreamUrl(partKey, state.serverUrl, state.token)
    }
}
