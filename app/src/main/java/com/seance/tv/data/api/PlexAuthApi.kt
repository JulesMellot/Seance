package com.seance.tv.data.api

import com.seance.tv.data.model.PinResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface PlexAuthApi {

    @Headers(
        "Accept: application/json",
        "X-Plex-Product: Seance",
        "X-Plex-Version: 1.0.0",
        "X-Plex-Platform: Android TV",
        "X-Plex-Device: Android TV"
    )
    @POST("pins")
    suspend fun createPin(
        @Header("X-Plex-Client-Identifier") clientId: String
    ): PinResponse

    @Headers("Accept: application/json")
    @GET("pins/{id}")
    suspend fun getPin(
        @Path("id") id: Long,
        @Header("X-Plex-Client-Identifier") clientId: String
    ): PinResponse
}
