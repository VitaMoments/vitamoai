package eu.vitamo.app.features.media.model

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import kotlin.uuid.Uuid

data class CreateMediaAssetInput(
    val ownerId: Uuid,
    val purpose: MediaPurpose,
    val visibility: MediaVisibility,
    val mimeType: String,
    val extension: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val sha256: String,
)