package com.seance.tv.data.repository

import com.seance.tv.data.api.PlexApi
import com.seance.tv.data.model.CollectionItem
import com.seance.tv.data.model.GenreEntry
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.LibrarySection
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.model.PlexConnection
import com.seance.tv.data.model.PlexDevice
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlexRepository @Inject constructor(
    private val plexApi: PlexApi,
    private val settingsRepository: SettingsRepository
) {
    suspend fun getLibrarySections(): List<LibrarySection> =
        plexApi.getLibrarySections().mediaContainer.sections

    /**
     * Sections films/séries activées dans les paramètres (toutes si aucun choix).
     * Optionnellement filtrées par [type] ("movie"/"show").
     */
    suspend fun getEnabledSections(type: String? = null): List<LibrarySection> {
        val enabled = settingsRepository.enabledLibraries.first()
        return getLibrarySections()
            .filter { it.type == "movie" || it.type == "show" }
            .filter { enabled.isEmpty() || it.key in enabled }
            .filter { type == null || it.type == type }
    }

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

    // ── Browse en grille ──────────────────────────────────────────────────
    suspend fun getSectionItems(
        sectionId: String,
        type: Int? = null,
        genre: String? = null,
        sort: String? = null,
        start: Int = 0,
        size: Int = 60
    ): List<MediaItem> =
        plexApi.getSectionAll(sectionId, type, genre, sort, start, size).mediaContainer.items

    suspend fun getSectionGenres(sectionId: String): List<GenreEntry> =
        plexApi.getSectionGenres(sectionId).mediaContainer.directory

    // ── Recherche multi-type ──────────────────────────────────────────────
    /** Items films/séries pertinents, à plat, dé-dupliqués. */
    suspend fun search(query: String): List<MediaItem> {
        if (query.isBlank()) return emptyList()
        val hubs = runCatching { plexApi.search(query).mediaContainer.hubs }.getOrDefault(emptyList())
        return hubs
            .flatMap { it.items }
            .filter { it.isMovie || it.isShow || it.isEpisode }
            .distinctBy { it.ratingKey }
    }

    // ── « Plus comme ça » ─────────────────────────────────────────────────
    /**
     * Similaires : d'abord les hubs Related de la metadata (includeRelated=1),
     * sinon repli sur le même genre dans la même bibliothèque.
     */
    suspend fun getSimilar(item: MediaItem): List<MediaItem> {
        val related = item.related?.hubs
            ?.flatMap { it.items }
            ?.filter { it.ratingKey != item.ratingKey }
            ?.distinctBy { it.ratingKey }
            ?: emptyList()
        if (related.isNotEmpty()) return related.take(20)

        val sectionId = item.librarySectionId?.toString() ?: return emptyList()
        val genreId = item.genres.firstOrNull()?.id ?: return emptyList()
        val type = if (item.isShow) 2 else 1
        return runCatching { getSectionItems(sectionId, type = type, genre = genreId.toString(), size = 24) }
            .getOrDefault(emptyList())
            .filter { it.ratingKey != item.ratingKey }
            .take(20)
    }

    suspend fun buildHome(): List<HomeRow> = coroutineScope {
        val sections = getEnabledSections()
        val mediaSection = sections.firstOrNull()

        val onDeckDeferred = async { getOnDeck() }

        val allCollections = sections
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

        // Rangées par genre (façon Netflix) — quelques genres au hasard sur les
        // sections films/séries, chargés en parallèle.
        val genreRowsDeferred = sections
            .filter { it.type == "movie" || it.type == "show" }
            .flatMap { section ->
                val genres = runCatching { getSectionGenres(section.key) }
                    .getOrDefault(emptyList())
                    .shuffled()
                    .take(2)
                val type = if (section.type == "show") 2 else 1
                genres.map { genre ->
                    async {
                        val items = runCatching {
                            getSectionItems(section.key, type = type, genre = genre.key, size = 20)
                        }.getOrDefault(emptyList())
                        HomeRow.Genre(title = genre.title, items = items)
                    }
                }
            }

        val rows = mutableListOf<HomeRow>()
        val onDeck = onDeckDeferred.await()
        if (onDeck.isNotEmpty()) rows.add(HomeRow.OnDeck(onDeck))

        val recentlyAdded = recentlyAddedDeferred?.await()
        if (!recentlyAdded.isNullOrEmpty()) rows.add(HomeRow.RecentlyAdded(recentlyAdded))

        val collectionRows = collectionRowsDeferred.awaitAll().filter { it.items.isNotEmpty() }
        collectionRows.forEach { rows.add(it) }

        // Dédoublonnage des rangées de genre par titre (un même genre peut exister
        // dans la lib Films ET Séries) — sinon clés LazyColumn en collision → crash.
        val usedTitles = collectionRows.map { it.title }.toMutableSet()
        genreRowsDeferred.awaitAll()
            .filter { it.items.size >= 4 }
            .forEach { row ->
                if (usedTitles.add(row.title)) rows.add(row)
            }

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
