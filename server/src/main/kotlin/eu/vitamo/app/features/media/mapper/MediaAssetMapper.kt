package eu.vitamo.app.features.media.mapper

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.features.media.entity.MediaAssetEntity
import eu.vitamo.app.features.media.model.MediaAssetRecord

fun MediaAssetEntity.toRecord(): MediaAssetRecord {
    return MediaAssetRecord(
        id = id.value,
        ownerId = ownerId,
        type = mediaType,
        purpose = purpose,
        status = status,
        visibility = visibility,
        storageKey = storageKey,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        sha256 = sha256,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}

fun MediaAssetRecord.toContract(): MediaAsset {
    return MediaAsset(
        id = id,
        type = type,
        purpose = purpose,
        status = status,
        visibility = visibility,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        contentPath = "/media/$id/content",
    )
}