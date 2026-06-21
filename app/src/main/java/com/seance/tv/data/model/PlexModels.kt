package com.seance.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Auth ---

@Serializable
data class PinResponse(
    val id: Long,
    val code: String,
    @SerialName("authToken") val authToken: String? = null
)

// --- Resources ---

@Serializable
data class ResourcesResponse(
    @SerialName("MediaContainer") val mediaContainer: ResourcesContainer? = null
)

@Serializable
data class ResourcesContainer(
    @SerialName("Device") val devices: List<PlexDevice> = emptyList()
)

@Serializable
data class PlexDevice(
    val name: String = "",
    val product: String = "",
    val provides: String = "",
    val connections: List<PlexConnection> = emptyList()
) {
    val isServer: Boolean get() = provides.contains("server")
}

@Serializable
data class PlexConnection(
    val uri: String = "",
    val local: Boolean = false,
    val relay: Boolean = false
) {
    val isLocal: Boolean get() = local
}

// --- Libraries ---

@Serializable
data class LibrarySectionsResponse(
    @SerialName("MediaContainer") val mediaContainer: LibrarySectionsContainer
)

@Serializable
data class LibrarySectionsContainer(
    @SerialName("Directory") val sections: List<LibrarySection> = emptyList()
)

@Serializable
data class LibrarySection(
    val key: String,
    val title: String,
    val type: String
)

// --- Media ---

@Serializable
data class MediaContainer(
    @SerialName("Metadata") val items: List<MediaItem> = emptyList(),
    val size: Int = 0
)

@Serializable
data class MediaContainerResponse(
    @SerialName("MediaContainer") val mediaContainer: MediaContainer
)

@Serializable
data class MediaItem(
    val ratingKey: String = "",
    val key: String = "",
    val title: String = "",
    val type: String = "",
    val summary: String = "",
    val year: Int? = null,
    val duration: Long? = null,
    val thumb: String? = null,
    val art: String? = null,
    val viewOffset: Long? = null,
    val viewCount: Int? = null,
    val rating: Double? = null,
    val contentRating: String? = null,
    val childCount: Int? = null,
    val tagline: String? = null,
    val studio: String? = null,
    val originallyAvailableAt: String? = null,
    @SerialName("librarySectionID") val librarySectionId: Int? = null,
    @SerialName("Director") val directors: List<PlexTag> = emptyList(),
    @SerialName("Genre") val genres: List<PlexTag> = emptyList(),
    @SerialName("Role") val cast: List<PlexRole> = emptyList(),
    @SerialName("Media") val media: List<PlexMedia> = emptyList(),
    @SerialName("Image") val images: List<PlexImage> = emptyList(),
    @SerialName("Related") val related: RelatedHubs? = null,
    @SerialName("index") val episodeIndex: Int? = null,
    @SerialName("parentIndex") val seasonIndex: Int? = null,
    @SerialName("parentTitle") val showTitle: String? = null,
    @SerialName("grandparentTitle") val showTitleForEpisode: String? = null,
    @SerialName("grandparentThumb") val showThumb: String? = null,
    @SerialName("leafCount") val leafCount: Int? = null,
    @SerialName("viewedLeafCount") val viewedLeafCount: Int? = null
) {
    val isMovie: Boolean get() = type == "movie"
    val isShow: Boolean get() = type == "show"
    val isEpisode: Boolean get() = type == "episode"
    val isSeason: Boolean get() = type == "season"
    val isCollection: Boolean get() = type == "collection"

    val progressFraction: Float
        get() {
            val offset = viewOffset ?: return 0f
            val total = duration ?: return 0f
            return (offset.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }

    // ── Libellés dérivés (champs fetchés mais jusqu'ici inutilisés en UI) ──
    val resolutionLabel: String?
        get() = media.firstOrNull()?.videoResolution?.let { res ->
            when (res.lowercase()) {
                "4k" -> "4K"
                "1080" -> "1080p"
                "720" -> "720p"
                "480" -> "480p"
                "sd" -> "SD"
                else -> res.uppercase()
            }
        }

    val videoCodecLabel: String? get() = media.firstOrNull()?.videoCodec?.uppercase()
    val audioCodecLabel: String? get() = media.firstOrNull()?.audioCodec?.uppercase()

    val runtimeLabel: String?
        get() = duration?.let {
            val h = it / 3_600_000
            val m = (it % 3_600_000) / 60_000
            if (h > 0) "${h}h ${m}min" else "${m}min"
        }

    val seasonsLabel: String?
        get() = childCount?.takeIf { isShow }?.let { "$it saison${if (it > 1) "s" else ""}" }

    val unwatchedCount: Int?
        get() {
            val leaf = leafCount ?: return null
            val viewed = viewedLeafCount ?: 0
            return (leaf - viewed).takeIf { it > 0 }
        }

    /** Logo-titre transparent (clearLogo) si le serveur le fournit. */
    val clearLogo: String?
        get() = images.firstOrNull { it.type == "clearLogo" }?.url
}

@Serializable
data class PlexImage(
    val alt: String = "",
    val type: String = "",
    val url: String = ""
)

@Serializable
data class PlexTag(val id: Int? = null, val tag: String = "")

@Serializable
data class RelatedHubs(
    @SerialName("Hub") val hubs: List<Hub> = emptyList()
)

@Serializable
data class PlexRole(
    val tag: String = "",
    val role: String = "",
    val thumb: String? = null
)

@Serializable
data class PlexMedia(
    val id: Long = 0,
    val duration: Long? = null,
    val bitrate: Int? = null,
    val videoCodec: String? = null,
    val audioCodec: String? = null,
    val videoResolution: String? = null,
    @SerialName("Part") val parts: List<PlexPart> = emptyList()
)

@Serializable
data class PlexPart(
    val id: Long = 0,
    val key: String = "",
    val duration: Long? = null,
    val file: String? = null,
    val size: Long? = null
)

// --- Collections ---

@Serializable
data class CollectionItem(
    val ratingKey: String = "",
    val key: String = "",
    val title: String = "",
    val thumb: String? = null,
    val art: String? = null,
    val childCount: Int? = null,
    val subtype: String? = null,
    val type: String = "collection"
)

@Serializable
data class CollectionContainerResponse(
    @SerialName("MediaContainer") val mediaContainer: CollectionContainer
)

@Serializable
data class CollectionContainer(
    @SerialName("Metadata") val collections: List<CollectionItem> = emptyList()
)

// --- Genres (chips de filtre browse) ---

@Serializable
data class GenreDirectoryResponse(
    @SerialName("MediaContainer") val mediaContainer: GenreContainer
)

@Serializable
data class GenreContainer(
    @SerialName("Directory") val directory: List<GenreEntry> = emptyList()
)

@Serializable
data class GenreEntry(
    val key: String = "",
    val title: String = ""
)

// --- Hubs (recherche multi-type + similaires) ---

@Serializable
data class HubContainerResponse(
    @SerialName("MediaContainer") val mediaContainer: HubContainer
)

@Serializable
data class HubContainer(
    @SerialName("Hub") val hubs: List<Hub> = emptyList()
)

@Serializable
data class Hub(
    val title: String = "",
    val type: String = "",
    val hubIdentifier: String = "",
    val size: Int = 0,
    @SerialName("Metadata") val items: List<MediaItem> = emptyList()
)

// --- Home rows ---

sealed class HomeRow {
    data class OnDeck(val items: List<MediaItem>) : HomeRow()
    data class Collection(val title: String, val collectionKey: String, val items: List<MediaItem>) : HomeRow()
    data class RecentlyAdded(val items: List<MediaItem>) : HomeRow()
    data class Genre(val title: String, val items: List<MediaItem>) : HomeRow()
    data class LiveTV(val items: List<MediaItem>) : HomeRow()
}
