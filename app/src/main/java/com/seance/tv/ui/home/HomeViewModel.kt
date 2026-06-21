package com.seance.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.repository.AuthRepository
import com.seance.tv.data.repository.PlexRepository
import com.seance.tv.di.ServerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val rows: List<HomeRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val featured: List<MediaItem> = emptyList(),  // hero auto-rotatif : reprendre + nouveautés
    val heroIndex: Int = 0,
    val focusedItem: MediaItem? = null,           // pour l'accent dynamique (carte focalisée)
    val refreshTick: Int = 0,                     // incrémenté à chaque « Découvrir d'autres choses »
    val serverUrl: String = "",
    val authToken: String = ""
) {
    val heroItem: MediaItem? get() = featured.getOrNull(heroIndex)
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plexRepository: PlexRepository,
    private val serverManager: ServerManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var rotationJob: Job? = null

    // Rangées épinglées (OnDeck/Nouveautés) + réserve (collections/genres) pour le cap + refresh
    private var pinnedRows: List<HomeRow> = emptyList()
    private var poolRows: List<HomeRow> = emptyList()
    private var poolOffset = 0
    private val visibleCap = 10

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
                    val onDeck = rows.filterIsInstance<HomeRow.OnDeck>().flatMap { it.items }
                    val recent = rows.filterIsInstance<HomeRow.RecentlyAdded>().flatMap { it.items }
                    val featured = interleave(onDeck.take(6), recent.take(6))
                        .distinctBy { it.ratingKey }
                        .take(8)
                    // Épinglées en tête (Continuer/Nouveautés) ; le reste alimente le cap + refresh.
                    pinnedRows = rows.filter { it is HomeRow.OnDeck || it is HomeRow.RecentlyAdded }
                    poolRows = rows.filterNot { it is HomeRow.OnDeck || it is HomeRow.RecentlyAdded }
                    poolOffset = 0
                    _uiState.update {
                        it.copy(rows = computeVisibleRows(), featured = featured, heroIndex = 0, isLoading = false)
                    }
                    startHeroRotation()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /** Le hero change tout seul toutes les 9 s s'il y a plusieurs éléments à la une. */
    private fun startHeroRotation() {
        rotationJob?.cancel()
        rotationJob = viewModelScope.launch {
            while (true) {
                delay(9_000)
                val count = _uiState.value.featured.size
                if (count > 1) {
                    _uiState.update { it.copy(heroIndex = (it.heroIndex + 1) % count) }
                }
            }
        }
    }

    /** Mélange alterné « reprendre » / « nouveautés ». */
    private fun interleave(a: List<MediaItem>, b: List<MediaItem>): List<MediaItem> {
        val out = ArrayList<MediaItem>(a.size + b.size)
        val max = maxOf(a.size, b.size)
        for (i in 0 until max) {
            if (i < a.size) out.add(a[i])
            if (i < b.size) out.add(b[i])
        }
        return out
    }

    /** Fenêtre visible = rangées épinglées + une tranche de la réserve (cap total). */
    private fun computeVisibleRows(): List<HomeRow> {
        if (poolRows.isEmpty()) return pinnedRows
        val n = (visibleCap - pinnedRows.size).coerceAtLeast(1).coerceAtMost(poolRows.size)
        val window = (0 until n).map { poolRows[(poolOffset + it) % poolRows.size] }
        return pinnedRows + window
    }

    /** « Découvrir d'autres choses » : avance dans la réserve (ou re-mélange si tout tient). */
    fun discoverMore() {
        val n = (visibleCap - pinnedRows.size).coerceAtLeast(1)
        if (poolRows.size <= n) {
            poolRows = poolRows.shuffled()
        } else {
            poolOffset = (poolOffset + n) % poolRows.size
        }
        _uiState.update { it.copy(rows = computeVisibleRows(), refreshTick = it.refreshTick + 1) }
    }

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

    override fun onCleared() {
        rotationJob?.cancel()
        super.onCleared()
    }
}
