package eu.vitamo.app.api.contracts.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

@Serializable
@SerialName("MEDIARESPONSE")
data class MediaAssetResponse(
    override val uuid : Uuid,
    override val referenceId: Uuid,
    override val referenceType: MediaReferenceType,
    override val purpose: MediaPurposeType,
    override val privacy: PrivacyStatus,
    override val contentType: String,
    override val sizeBytes: Long,
    override val url: String,
    override val metadata: JsonElement? = null
): MediaAsset