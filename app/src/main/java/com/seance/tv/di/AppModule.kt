package com.seance.tv.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.seance.tv.BuildConfig
import com.seance.tv.data.api.PlexApi
import com.seance.tv.data.api.PlexAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    // Plain client — no Plex headers, used for PIN auth endpoints
    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
                    else HttpLoggingInterceptor.Level.NONE
            redactHeader("X-Plex-Token")
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    // Plex client — carries all X-Plex-* headers on every request
    @Provides
    @Singleton
    @Named("plex")
    fun providePlexOkHttpClient(headersInterceptor: PlexHeadersInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
                    else HttpLoggingInterceptor.Level.NONE
            redactHeader("X-Plex-Token")
        }
        return OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("plain") client: OkHttpClient): PlexAuthApi =
        Retrofit.Builder()
            .baseUrl("https://plex.tv/api/v2/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PlexAuthApi::class.java)

    // Default PlexApi — points at localhost until ServerManager updates the real URL.
    // In practice, ServerManager.buildPlexApi() is used to create a correctly-scoped instance.
    @Provides
    @Singleton
    fun providePlexApi(@Named("plex") client: OkHttpClient): PlexApi =
        Retrofit.Builder()
            .baseUrl("http://localhost:32400/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PlexApi::class.java)
}
