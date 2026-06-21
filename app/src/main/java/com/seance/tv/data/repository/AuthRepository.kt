package com.seance.tv.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seance.tv.data.api.PlexAuthApi
import com.seance.tv.data.model.HomeUser
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
        private val KEY_ADMIN_TOKEN = stringPreferencesKey("plex_admin_token")
        private val KEY_CLIENT_ID = stringPreferencesKey("plex_client_id")
    }

    /** Token actif (profil en cours) — utilisé par toute l'app. */
    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }

    /** Token du compte admin (Plex Home) — sert à lister/basculer les profils. */
    val adminToken: Flow<String?> = context.dataStore.data.map { it[KEY_ADMIN_TOKEN] }

    suspend fun getOrCreateClientId(): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_CLIENT_ID] ?: run {
            val newId = UUID.randomUUID().toString()
            context.dataStore.edit { it[KEY_CLIENT_ID] = newId }
            newId
        }
    }

    suspend fun createPin(): PinResponse = authApi.createPin(getOrCreateClientId())

    suspend fun pollForToken(pinId: Long): String? {
        val clientId = getOrCreateClientId()
        repeat(150) {
            delay(2_000)
            val pin = authApi.getPin(pinId, clientId)
            if (!pin.authToken.isNullOrBlank()) {
                saveToken(pin.authToken)
                return pin.authToken
            }
        }
        return null
    }

    /** À la connexion PIN, le token obtenu est à la fois admin et actif. */
    private suspend fun saveToken(token: String) {
        context.dataStore.edit {
            it[KEY_AUTH_TOKEN] = token
            it[KEY_ADMIN_TOKEN] = token
        }
    }

    /**
     * Migration douce : installs antérieurs aux profils n'ont qu'un token actif
     * (qui est en fait le token admin). On le recopie comme token admin.
     */
    suspend fun ensureAdminToken() {
        val prefs = context.dataStore.data.first()
        if (prefs[KEY_ADMIN_TOKEN].isNullOrBlank() && !prefs[KEY_AUTH_TOKEN].isNullOrBlank()) {
            context.dataStore.edit { it[KEY_ADMIN_TOKEN] = prefs[KEY_AUTH_TOKEN]!! }
        }
    }

    /** Profils Plex Home du compte (vide si compte simple / échec). */
    suspend fun getHomeUsers(): List<HomeUser> {
        val admin = adminToken.first() ?: authToken.first() ?: return emptyList()
        val clientId = getOrCreateClientId()
        return runCatching { authApi.getHomeUsers(clientId, admin).users }.getOrDefault(emptyList())
    }

    /** Bascule sur un profil : récupère son token et le rend actif. */
    suspend fun switchProfile(uuid: String, pin: String?): String? {
        val admin = adminToken.first() ?: authToken.first() ?: return null
        val clientId = getOrCreateClientId()
        val user = authApi.switchHomeUser(uuid, clientId, admin, pin)
        val token = user.authToken ?: return null
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
        return token
    }

    /** Rend le token admin actif (profil admin sans PIN). */
    suspend fun useAdminAsActive() {
        val admin = adminToken.first() ?: return
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = admin }
    }

    /** Définit le token actif (ex. token d'accès serveur propre au profil). */
    suspend fun setActiveToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(KEY_AUTH_TOKEN)
            it.remove(KEY_ADMIN_TOKEN)
        }
    }
}
