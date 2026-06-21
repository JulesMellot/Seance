package com.seance.tv.data.api

import com.seance.tv.data.model.PlexDevice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface PlexResourcesApi {

    @GET("resources")
    suspend fun getResources(
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 1
    ): List<PlexDevice>
}
