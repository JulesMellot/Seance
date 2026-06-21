package com.seance.tv.data.repository

import com.seance.tv.data.api.PlexApi
import com.seance.tv.data.model.CollectionItem
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.LibrarySection
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.model.PlexConnection
import com.seance.tv.data.model.PlexDevice
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlexRepository @Inject constructor(
    private val plexApi: PlexApi
) {
    suspend fun getLibrarySections(): List<LibrarySection> =
        plexApi.getLibrarySections().mediaContainer.sections

    suspend fun getOnDeck(): List<MediaItem> =
        plexApi.getOnDeck().mediaContainer.items

    suspend fun getCollections(sectionId: String): List<CollectionItem> =
        plexApi.getCollections(sectionId).mediaContainer.collections

    suspend fun getCollectionItems(collectionId: String): List<MediaItem> =
        plexApi.getCollectionItems(collectionId).mediaContainer.items

    suspend fun getRecentlyAdded(sectionId: String): List<MediaItem> =
        plexApi.getRecentlyAdded(sectionId).mediaContainer.items

    suspend fun getMetadata(ratingKey: String): MediaItem? =
        plexApi.getMetadata(ratingKey).mediaContainer.items.firstOrNull()

    suspend fun getChildren(ratingKey: String): List<MediaItem> =
        plexApi.getChildren(ratingKey).mediaContainer.items

    suspend fun buildHome(): List<HomeRow> = coroutineScope {
        val sections = getLibrarySections()
        val mediaSection = sections.firstOrNull { it.type == "movie" || it.type == "show" }

        val onDeckDeferred = async { getOnDeck() }

        val allCollections = sections
            .filter { it.type == "movie" || it.type == "show" }
            .flatMap { section ->
                runCatching { getCollections(section.key) }.getOrDefault(emptyList())
            }
            .shuffled()
            .take(8)

        val collectionRowsDeferred = allCollections.map { collection ->
            async {
                val items = runCatching { getCollectionItems(collection.ratingKey) }.getOrDefault(emptyList())
                HomeRow.Collection(
                    title = collection.title,
                    collectionKey = collection.ratingKey,
                    items = items
                )
            }
        }

        val recentlyAddedDeferred = mediaSection?.let {
            async { getRecentlyAdded(it.key) }
        }

        val rows = mutableListOf<HomeRow>()
        val onDeck = onDeckDeferred.await()
        if (onDeck.isNotEmpty()) rows.add(HomeRow.OnDeck(onDeck))

        collectionRowsDeferred.awaitAll()
            .filter { it.items.isNotEmpty() }
            .forEach { rows.add(it) }

        val recentlyAdded = recentlyAddedDeferred?.await()
        if (!recentlyAdded.isNullOrEmpty()) rows.add(HomeRow.RecentlyAdded(recentlyAdded))

        rows
    }

    suspend fun reportProgress(ratingKey: String, key: String, timeMs: Long, durationMs: Long) {
        runCatching { plexApi.reportProgress(ratingKey, key, timeMs, durationMs) }
    }

    fun buildStreamUrl(partKey: String, serverBaseUrl: String, token: String): String =
        "$serverBaseUrl$partKey?X-Plex-Token=$token"

    fun buildImageUrl(path: String?, serverBaseUrl: String, token: String): String? =
        path?.let { "$serverBaseUrl$it?X-Plex-Token=$token" }

    fun selectBestConnection(device: PlexDevice): PlexConnection? =
        device.connections
            .sortedWith(compareByDescending<PlexConnection> { it.isLocal }.thenBy { it.relay })
            .firstOrNull()
}
