package eu.vitamo.app.api.contracts.media

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
enum class MediaType {
    IMAGE,
    VIDEO,
}

@Serializable
enum class MediaPurpose {
    PROFILE_IMAGE,
    FEED_ATTACHMENT,
}

@Serializable
enum class MediaStatus {
    PENDING,
    READY,
    FAILED,
    DELETED,
}

@Serializable
enum class MediaVisibility {
    PUBLIC,
    AUTHENTICATED,
    FRIENDS,
    PRIVATE,
}

@Serializable
data class MediaAsset(
    @Contextual
    val id: Uuid,
    val type: MediaType,
    val purpose: MediaPurpose,
    val status: MediaStatus,
    val visibility: MediaVisibility,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int?,
    val height: Int?,
    val contentPath: String,
)

@Serializable
data class MediaReference(
    @Contextual
    val id: Uuid,
    val contentPath: String,
)

fun MediaAsset.toMediaReference(): MediaReference =
    MediaReference(
        id = id,
        contentPath = contentPath,
    )