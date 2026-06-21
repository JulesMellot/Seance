package com.seance.tv.di

import android.content.Context
import com.seance.tv.data.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlexHeadersInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val clientId = runBlocking { authRepository.getOrCreateClientId() }
        val token = runBlocking { authRepository.authToken.first() }

        val request = chain.request().newBuilder()
            .addHeader("X-Plex-Client-Identifier", clientId)
            .addHeader("X-Plex-Product", "Séance")
            .addHeader("X-Plex-Version", "1.0.0")
            .addHeader("X-Plex-Platform", "Android TV")
            .addHeader("X-Plex-Device", "Android TV")
            .addHeader("Accept", "application/json")
            .apply {
                if (!token.isNullOrBlank()) addHeader("X-Plex-Token", token)
            }
            .build()

        return chain.proceed(request)
    }
}
