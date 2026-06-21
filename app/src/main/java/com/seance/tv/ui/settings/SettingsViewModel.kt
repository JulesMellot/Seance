package com.seance.tv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.LibrarySection
import com.seance.tv.data.repository.PlexRepository
import com.seance.tv.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryToggle(
    val section: LibrarySection,
    val enabled: Boolean
)

data class SettingsUiState(
    val libraries: List<LibraryToggle> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var allMediaSections: List<LibrarySection> = emptyList()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                allMediaSections = plexRepository.getLibrarySections()
                    .filter { it.type == "movie" || it.type == "show" }
                val enabled = settingsRepository.enabledLibraries.first()
                _uiState.update { it.copy(libraries = toToggles(enabled), isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** Ensemble vide en stockage = toutes activées. */
    private fun toToggles(enabled: Set<String>): List<LibraryToggle> =
        allMediaSections.map { LibraryToggle(it, enabled.isEmpty() || it.key in enabled) }

    fun toggle(sectionKey: String) {
        viewModelScope.launch {
            val saved = settingsRepository.enabledLibraries.first()
            val allKeys = allMediaSections.map { it.key }.toSet()
            val effective = if (saved.isEmpty()) allKeys else saved
            val next = if (sectionKey in effective) effective - sectionKey else effective + sectionKey
            if (next.isEmpty()) return@launch  // garder au moins une bibliothèque
            // Si tout est activé, on stocke un ensemble vide (= toutes, robuste aux ajouts futurs).
            settingsRepository.setEnabledLibraries(if (next == allKeys) emptySet() else next)
            _uiState.update { it.copy(libraries = toToggles(if (next == allKeys) emptySet() else next)) }
        }
    }
}
