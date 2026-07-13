package eu.vitamo.app.api.contracts.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import kotlin.uuid.Uuid

data class MediaCreateInput(
    val uuid: Uuid,
    val referenceId: Uuid,
    val referenceType: MediaReferenceType,
    val purpose: MediaPurposeType,
    val privacy: PrivacyStatus,
    val originalFileName: String?,
    val storedFileName: String,
    val objectKey: String,
    val contentType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val createdBy: Uuid?
)
