package com.seance.tv.data.api

import com.seance.tv.data.model.CollectionContainerResponse
import com.seance.tv.data.model.GenreDirectoryResponse
import com.seance.tv.data.model.HubContainerResponse
import com.seance.tv.data.model.LibrarySectionsResponse
import com.seance.tv.data.model.MediaContainerResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PlexApi {

    @GET("library/sections")
    suspend fun getLibrarySections(): LibrarySectionsResponse

    @GET("library/sections/{sectionId}/collections")
    suspend fun getCollections(
        @Path("sectionId") sectionId: String
    ): CollectionContainerResponse

    @GET("library/collections/{collectionId}/children")
    suspend fun getCollectionItems(
        @Path("collectionId") collectionId: String
    ): MediaContainerResponse

    @GET("library/onDeck")
    suspend fun getOnDeck(): MediaContainerResponse

    @GET("library/sections/{sectionId}/recentlyAdded")
    suspend fun getRecentlyAdded(
        @Path("sectionId") sectionId: String,
        @Query("X-Plex-Container-Start") start: Int = 0,
        @Query("X-Plex-Container-Size") size: Int = 20
    ): MediaContainerResponse

    @GET("library/sections/{sectionId}/all")
    suspend fun getSectionAll(
        @Path("sectionId") sectionId: String,
        @Query("type") type: Int? = null,            // 1 = film, 2 = série
        @Query("genre") genre: String? = null,        // id de genre (depuis getSectionGenres)
        @Query("sort") sort: String? = null,          // ex. "titleSort", "addedAt:desc"
        @Query("unwatched") unwatched: Int? = null,    // 1 = uniquement les non-vus
        @Query("X-Plex-Container-Start") start: Int = 0,
        @Query("X-Plex-Container-Size") size: Int = 60
    ): MediaContainerResponse

    @GET("library/sections/{sectionId}/genre")
    suspend fun getSectionGenres(
        @Path("sectionId") sectionId: String
    ): GenreDirectoryResponse

    @GET("hubs/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("limit") limit: Int = 30
    ): HubContainerResponse

    @GET("library/metadata/{ratingKey}")
    suspend fun getMetadata(
        @Path("ratingKey") ratingKey: String,
        @Query("includeRelated") includeRelated: Int = 1,
        @Query("includeExtras") includeExtras: Int = 0
    ): MediaContainerResponse

    @GET("library/metadata/{ratingKey}/children")
    suspend fun getChildren(
        @Path("ratingKey") ratingKey: String
    ): MediaContainerResponse

    @POST(":/progress")
    suspend fun reportProgress(
        @Query("ratingKey") ratingKey: String,
        @Query("key") key: String,
        @Query("time") timeMs: Long,
        @Query("duration") durationMs: Long,
        @Query("state") state: String = "playing",
        @Query("hasMDE") hasMDE: Int = 1
    )

    @GET
    suspend fun getResourcesRaw(@Url url: String): MediaContainerResponse
}
