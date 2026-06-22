package com.seance.tv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.repository.PlexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val title: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val showControls: Boolean = true,
    val streamUrl: String = "",
    val ratingKey: String = "",
    val mediaKey: String = "",
    val audioLanguage: String? = null,
    val subtitleLanguage: String? = null,
    val subtitlesDisabled: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val plexRepository: PlexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    fun initialize(
        ratingKey: String,
        title: String,
        streamUrl: String,
        partKey: String?,
        durationMs: Long,
        viewOffsetMs: Long,
        audioLanguage: String? = null,
        subtitleLanguage: String? = null,
        subtitlesDisabled: Boolean = false
    ) {
        _uiState.update {
            it.copy(
                ratingKey = ratingKey,
                title = title,
                streamUrl = streamUrl,
                durationMs = durationMs,
                positionMs = viewOffsetMs,
                isPlaying = true,
                audioLanguage = audioLanguage,
                subtitleLanguage = subtitleLanguage,
                subtitlesDisabled = subtitlesDisabled
            )
        }
        startProgressReporting(ratingKey)
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seek(offsetMs: Long) {
        _uiState.update {
            val newPos = (it.positionMs + offsetMs).coerceIn(0L, it.durationMs)
            it.copy(positionMs = newPos)
        }
    }

    fun updatePosition(positionMs: Long) {
        _uiState.update { it.copy(positionMs = positionMs) }
    }

    fun onProgress(positionMs: Long, durationMs: Long) {
        _uiState.update {
            it.copy(
                positionMs = positionMs.coerceAtLeast(0L),
                durationMs = if (durationMs > 0L) durationMs else it.durationMs
            )
        }
    }

    fun saveProgress() {
        val state = _uiState.value
        if (state.ratingKey.isBlank()) return
        viewModelScope.launch {
            plexRepository.reportProgress(
                ratingKey = state.ratingKey,
                key = "/library/metadata/${state.ratingKey}",
                timeMs = state.positionMs,
                durationMs = state.durationMs
            )
        }
    }

    private fun startProgressReporting(ratingKey: String) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                saveProgress()
            }
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        saveProgress()
        super.onCleared()
    }
}
