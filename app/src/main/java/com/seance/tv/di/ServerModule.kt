package com.seance.tv.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seance.tv.data.model.PlexDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverDataStore by preferencesDataStore(name = "server_prefs")

@Singleton
class ServerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
    }

    val serverUrl: Flow<String?> = context.serverDataStore.data.map { it[KEY_SERVER_URL] }

    suspend fun saveServerUrl(url: String) {
        context.serverDataStore.edit { it[KEY_SERVER_URL] = url }
    }

    fun selectBestServerUrl(device: PlexDevice): String? =
        device.connections
            .sortedWith(compareByDescending { it.isLocal })
            .firstOrNull()?.uri
}
