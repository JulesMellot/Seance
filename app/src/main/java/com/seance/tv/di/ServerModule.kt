package com.seance.tv.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.seance.tv.data.api.PlexApi
import com.seance.tv.data.model.PlexDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private val Context.serverDataStore by preferencesDataStore(name = "server_prefs")

@Singleton
class ServerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("plex") private val okHttpClient: OkHttpClient
) {
    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    val serverUrl: Flow<String?> = context.serverDataStore.data.map { it[KEY_SERVER_URL] }

    suspend fun saveServerUrl(url: String) {
        context.serverDataStore.edit { it[KEY_SERVER_URL] = url }
    }

    fun buildPlexApi(baseUrl: String): PlexApi {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PlexApi::class.java)
    }

    fun selectBestServerUrl(device: PlexDevice): String? =
        device.connections
            .sortedWith(compareByDescending { it.isLocal })
            .firstOrNull()?.uri
}
