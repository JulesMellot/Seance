package com.seance.tv.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

/**
 * Préférences utilisateur. Pour l'instant : choix des bibliothèques Plex affichées.
 *
 * Sémantique : un ensemble VIDE signifie « toutes les bibliothèques » (défaut).
 * Sinon, seules les sections dont la clé est présente sont affichées.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ENABLED_LIBRARIES = stringSetPreferencesKey("enabled_libraries")
        private val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        private const val RECENT_DELIM = "\n"
        private const val RECENT_MAX = 8
    }

    val enabledLibraries: Flow<Set<String>> =
        context.settingsDataStore.data.map { it[KEY_ENABLED_LIBRARIES] ?: emptySet() }

    suspend fun setEnabledLibraries(keys: Set<String>) {
        context.settingsDataStore.edit { it[KEY_ENABLED_LIBRARIES] = keys }
    }

    /** Recherches récentes, de la plus récente à la plus ancienne. */
    val recentSearches: Flow<List<String>> =
        context.settingsDataStore.data.map { prefs ->
            prefs[KEY_RECENT_SEARCHES]
                ?.split(RECENT_DELIM)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        context.settingsDataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_SEARCHES]
                ?.split(RECENT_DELIM)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val updated = (listOf(trimmed) + current.filter { !it.equals(trimmed, ignoreCase = true) })
                .take(RECENT_MAX)
            prefs[KEY_RECENT_SEARCHES] = updated.joinToString(RECENT_DELIM)
        }
    }

    suspend fun clearRecentSearches() {
        context.settingsDataStore.edit { it.remove(KEY_RECENT_SEARCHES) }
    }
}
