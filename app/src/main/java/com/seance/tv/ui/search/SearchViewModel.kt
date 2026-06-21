package com.seance.tv.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.repository.AuthRepository
import com.seance.tv.data.repository.PlexRepository
import com.seance.tv.data.repository.SettingsRepository
import com.seance.tv.di.ServerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val serverUrl: String = "",
    val token: String = ""
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val serverManager: ServerManager,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<String>> =
        settingsRepository.recentSearches.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val serverUrl = serverManager.serverUrl.first() ?: ""
            val token = authRepository.authToken.first() ?: ""
            _uiState.update { it.copy(serverUrl = serverUrl, token = token) }
        }
    }

    fun append(char: Char) = setQuery(_uiState.value.query + char)
    fun backspace() = setQuery(_uiState.value.query.dropLast(1))
    fun clear() = setQuery("")

    /** Relance une recherche depuis une suggestion récente. */
    fun useRecent(query: String) = setQuery(query)

    /** Remplit le champ avec le résultat de la reconnaissance vocale. */
    fun voiceResult(text: String) = setQuery(text)

    /** Mémorise la requête courante quand l'utilisateur ouvre un résultat. */
    fun onResultOpened() {
        val q = _uiState.value.query
        viewModelScope.launch { settingsRepository.addRecentSearch(q) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { settingsRepository.clearRecentSearches() }
    }

    private fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false, hasSearched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(280) // anti-rebond
            _uiState.update { it.copy(isSearching = true) }
            val results = runCatching { plexRepository.search(query) }.getOrDefault(emptyList())
            _uiState.update { it.copy(results = results, isSearching = false, hasSearched = true) }
        }
    }

    fun imageUrl(path: String?): String? {
        val state = _uiState.value
        return plexRepository.buildImageUrl(path, state.serverUrl, state.token)
    }
}
