package com.seance.tv.data.api

import com.seance.tv.data.model.PlexDevice
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface PlexResourcesApi {

    @Headers("Accept: application/json")
    @GET("resources")
    suspend fun getResources(
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Query("X-Plex-Token") token: String,
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 1
    ): List<PlexDevice>
}
