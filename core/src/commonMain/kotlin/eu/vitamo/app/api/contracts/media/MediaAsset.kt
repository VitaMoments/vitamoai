package eu.vitamo.app.api.contracts.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

@Serializable
sealed interface MediaAsset {
    val uuid: Uuid
    val url: String
    val contentType: String
    val sizeBytes: Long
    val purpose: MediaPurposeType
    val privacy: PrivacyStatus
    val referenceType: MediaReferenceType
    val referenceId: Uuid
    val metadata: JsonElement?
}