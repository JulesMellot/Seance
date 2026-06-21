package com.seance.tv.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.GenreEntry
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

data class BrowseUiState(
    val title: String = "",
    val genres: List<GenreEntry> = emptyList(),
    val selectedGenre: GenreEntry? = null,   // null = « Tous »
    val items: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isPaging: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val serverUrl: String = "",
    val token: String = ""
)

private const val PAGE = 60

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val serverManager: ServerManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private var sectionId: String? = null
    private var plexType: Int = 1
    private var started = false

    fun start(sectionType: String) {
        if (started) return
        started = true
        plexType = if (sectionType == "show") 2 else 1
        viewModelScope.launch {
            val serverUrl = serverManager.serverUrl.first() ?: ""
            val token = authRepository.authToken.first() ?: ""
            _uiState.update {
                it.copy(
                    title = if (sectionType == "show") "Séries" else "Films",
                    serverUrl = serverUrl,
                    token = token
                )
            }
            runCatching {
                val section = plexRepository.getEnabledSections(sectionType).firstOrNull()
                    ?: plexRepository.getLibrarySections().firstOrNull { it.type == sectionType }
                    ?: error("Aucune bibliothèque ${if (sectionType == "show") "séries" else "films"}")
                sectionId = section.key
                val genres = runCatching { plexRepository.getSectionGenres(section.key) }.getOrDefault(emptyList())
                _uiState.update { it.copy(genres = genres) }
                loadPage(reset = true)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectGenre(genre: GenreEntry?) {
        if (genre?.key == _uiState.value.selectedGenre?.key) return
        _uiState.update { it.copy(selectedGenre = genre, items = emptyList(), endReached = false, isLoading = true) }
        viewModelScope.launch { loadPage(reset = true) }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isPaging || s.endReached || s.isLoading) return
        viewModelScope.launch { loadPage(reset = false) }
    }

    private suspend fun loadPage(reset: Boolean) {
        val id = sectionId ?: return
        val start = if (reset) 0 else _uiState.value.items.size
        _uiState.update { if (reset) it.copy(isLoading = true) else it.copy(isPaging = true) }
        runCatching {
            plexRepository.getSectionItems(
                sectionId = id,
                type = plexType,
                genre = _uiState.value.selectedGenre?.key,
                sort = "titleSort",
                start = start,
                size = PAGE
            )
        }.onSuccess { page ->
            _uiState.update {
                val merged = if (reset) page else it.items + page
                it.copy(
                    items = merged,
                    isLoading = false,
                    isPaging = false,
                    endReached = page.size < PAGE
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, isPaging = false, error = e.message) }
        }
    }

    fun imageUrl(path: String?): String? {
        val state = _uiState.value
        return plexRepository.buildImageUrl(path, state.serverUrl, state.token)
    }
}
