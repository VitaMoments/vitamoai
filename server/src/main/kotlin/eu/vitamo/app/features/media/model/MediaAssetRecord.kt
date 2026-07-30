package eu.vitamo.app.features.media.model

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.media.MediaType
import eu.vitamo.app.api.contracts.media.MediaVisibility
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class MediaAssetRecord(
    val id: Uuid,
    val ownerId: Uuid,
    val type: MediaType,
    val purpose: MediaPurpose,
    val status: MediaStatus,
    val visibility: MediaVisibility,
    val storageKey: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int?,
    val height: Int?,
    val sha256: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)