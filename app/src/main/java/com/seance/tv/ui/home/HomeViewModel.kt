package com.seance.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.repository.PlexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val rows: List<HomeRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val focusedItem: MediaItem? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plexRepository: PlexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
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
}
