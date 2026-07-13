package eu.vitamo.app.api.contracts.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class MediaAssetContext(
    override val uuid : Uuid,
    override val referenceId: Uuid,
    override val referenceType: MediaReferenceType,
    override val purpose: MediaPurposeType,
    override val privacy: PrivacyStatus,
    override val contentType: String,
    override val sizeBytes: Long,
    override val url: String,
    override val metadata: JsonElement? = null,

    val originalFileName: String? = null,
    val storedFileName: String,
    val objectKey: String,
    val width: Int? = null,
    val height: Int? = null,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
    @Serializable(with = InstantSerializer::class) val deletedAt: Instant? = null,
    val createdBy: Uuid,
) : MediaAsset