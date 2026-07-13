package eu.vitamo.app.features.media.mapper

import eu.vitamo.app.api.contracts.media.MediaAssetContext
import eu.vitamo.app.database.helpers.kotlinUuid
import eu.vitamo.app.features.media.entity.MediaAssetEntity
import kotlin.time.Instant
import kotlin.uuid.toKotlinUuid

fun MediaAssetEntity.toMediaAsset(): MediaAssetContext =
    MediaAssetContext(
        uuid = this.kotlinUuid,
        referenceId = referenceId,
        referenceType = referenceType,
        purpose = purpose,
        privacy = privacy,
        originalFileName = originalFileName,
        storedFileName = storedFileName,
        objectKey = objectKey,
        contentType = contentType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        deletedAt = deletedAt?.let { Instant.fromEpochMilliseconds(it) },
        url = "/api/media/${this.kotlinUuid}",
        createdBy = createdBy.value,
    )