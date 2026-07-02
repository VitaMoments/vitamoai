package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemMediaAssetsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemMediaAssetEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<FeedItemMediaAssetEntity>(FeedItemMediaAssetsTable)

    var feedItem by FeedItemEntity referencedOn FeedItemMediaAssetsTable.feedItem
    var mediaAssetId by FeedItemMediaAssetsTable.mediaAssetId
    var mediaAssetUrl by FeedItemMediaAssetsTable.mediaAssetUrl
    var mediaAssetType by FeedItemMediaAssetsTable.mediaAssetType
    var mediaAssetMetadata by FeedItemMediaAssetsTable.mediaAssetMetadata
}
