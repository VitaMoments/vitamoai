package eu.vitamo.app.features.feed.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemMediaAssetsTable : UuidTable(name = "feed_item_media_assets") {
    val feedItem = reference(
        name = "feed_item_id",
        foreign = FeedItemsTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val mediaAssetId = uuid(name = "media_asset_id")
    val mediaAssetUrl = text(name = "media_asset_url")
    val mediaAssetType = varchar(name = "media_asset_type", length = 32)
    val mediaAssetMetadata = text(name = "media_asset_metadata").nullable()

    init {
        uniqueIndex(customIndexName = "uidx_feed_item_media_assets_item_asset", columns = arrayOf(feedItem, mediaAssetId))
        index(customIndexName = "idx_feed_item_media_assets_feed_item", isUnique = false, columns = arrayOf(feedItem))
    }
}
