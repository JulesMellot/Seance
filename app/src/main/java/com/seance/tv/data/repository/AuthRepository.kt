package com.seance.tv.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seance.tv.data.api.PlexAuthApi
import com.seance.tv.data.model.PinResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seance_prefs")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: PlexAuthApi
) {
    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("plex_auth_token")
        private val KEY_CLIENT_ID = stringPreferencesKey("plex_client_id")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }

    suspend fun getOrCreateClientId(): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_CLIENT_ID] ?: run {
            val newId = UUID.randomUUID().toString()
            context.dataStore.edit { it[KEY_CLIENT_ID] = newId }
            newId
        }
    }

    suspend fun createPin(): PinResponse = authApi.createPin()

    suspend fun pollForToken(pinId: Long): String? {
        repeat(150) {
            delay(2_000)
            val pin = authApi.getPin(pinId)
            if (!pin.authToken.isNullOrBlank()) {
                saveToken(pin.authToken)
                return pin.authToken
            }
        }
        return null
    }

    private suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(KEY_AUTH_TOKEN) }
    }
}
