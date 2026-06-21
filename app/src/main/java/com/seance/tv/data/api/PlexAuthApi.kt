package com.seance.tv.data.api

import com.seance.tv.data.model.PinResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface PlexAuthApi {

    @POST("pins")
    suspend fun createPin(): PinResponse

    @GET("pins/{id}")
    suspend fun getPin(@Path("id") id: Long): PinResponse
}
