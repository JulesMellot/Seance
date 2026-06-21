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
    @SerialName("provides") val provides: String = "",
    @SerialName("Connection") val connections: List<PlexConnection> = emptyList()
) {
    val isServer: Boolean get() = provides.contains("server")
}

@Serializable
data class PlexConnection(
    val uri: String = "",
    val local: Int = 0,
    val relay: Int = 0
) {
    val isLocal: Boolean get() = local == 1
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
    @SerialName("Director") val directors: List<PlexTag> = emptyList(),
    @SerialName("Genre") val genres: List<PlexTag> = emptyList(),
    @SerialName("Role") val cast: List<PlexRole> = emptyList(),
    @SerialName("Media") val media: List<PlexMedia> = emptyList(),
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
}

@Serializable
data class PlexTag(val tag: String = "")

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

// --- Home rows ---

sealed class HomeRow {
    data class OnDeck(val items: List<MediaItem>) : HomeRow()
    data class Collection(val title: String, val collectionKey: String, val items: List<MediaItem>) : HomeRow()
    data class RecentlyAdded(val items: List<MediaItem>) : HomeRow()
    data class LiveTV(val items: List<MediaItem>) : HomeRow()
}
