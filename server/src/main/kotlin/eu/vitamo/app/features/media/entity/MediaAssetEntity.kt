package eu.vitamo.app.features.media.entity

import eu.vitamo.app.features.media.table.MediaAssetsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class MediaAssetEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object :
        UuidEntityClass<MediaAssetEntity>(MediaAssetsTable)

    var ownerId by MediaAssetsTable.ownerId

    var feedItemId by MediaAssetsTable.feedItemId
    var position by MediaAssetsTable.position

    var mediaType by MediaAssetsTable.mediaType
    var purpose by MediaAssetsTable.purpose
    var status by MediaAssetsTable.status
    var visibility by MediaAssetsTable.visibility

    var storageKey by MediaAssetsTable.storageKey
    var mimeType by MediaAssetsTable.mimeType
    var sizeBytes by MediaAssetsTable.sizeBytes

    var width by MediaAssetsTable.width
    var height by MediaAssetsTable.height

    var sha256 by MediaAssetsTable.sha256

    var createdAt by MediaAssetsTable.createdAt
    var updatedAt by MediaAssetsTable.updatedAt
    var deletedAt by MediaAssetsTable.deletedAt
}