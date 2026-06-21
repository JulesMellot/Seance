package com.seance.tv.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.repository.PlexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val item: MediaItem? = null,
    val seasons: List<MediaItem> = emptyList(),
    val selectedSeason: MediaItem? = null,
    val episodes: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val serverUrl: String = "",
    val token: String = ""
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val plexRepository: PlexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(ratingKey: String, serverUrl: String, token: String) {
        _uiState.update { it.copy(serverUrl = serverUrl, token = token) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val item = plexRepository.getMetadata(ratingKey) ?: error("Média introuvable")
                val children = plexRepository.getChildren(ratingKey)
                when {
                    item.isShow -> {
                        val seasons = children.filter { it.isSeason }
                        val firstSeason = seasons.firstOrNull()
                        val episodes = firstSeason?.let { plexRepository.getChildren(it.ratingKey) } ?: emptyList()
                        _uiState.update {
                            it.copy(item = item, seasons = seasons, selectedSeason = firstSeason, episodes = episodes, isLoading = false)
                        }
                    }
                    else -> _uiState.update { it.copy(item = item, isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectSeason(season: MediaItem) {
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
}
