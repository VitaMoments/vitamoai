package eu.vitamo.app.features.feed.table

import eu.vitamo.app.api.contracts.feed.FeedCategory
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemCategoriesTable : UuidTable(name = "feed_item_categories") {
    val feedItem = reference(
        name = "feed_item_id",
        foreign = FeedItemsTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val category = enumerationByName(name = "category", length = 64, klass = FeedCategory::class)

    init {
        uniqueIndex(customIndexName = "uidx_feed_item_categories_item_category", columns = arrayOf(feedItem, category))
        index(customIndexName = "idx_feed_item_categories_feed_item", isUnique = false, columns = arrayOf(feedItem))
    }
}
