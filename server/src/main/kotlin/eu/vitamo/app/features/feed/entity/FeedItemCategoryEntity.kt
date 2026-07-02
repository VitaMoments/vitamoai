package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemCategoriesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemCategoryEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<FeedItemCategoryEntity>(FeedItemCategoriesTable)

    var feedItem by FeedItemEntity referencedOn FeedItemCategoriesTable.feedItem
    var category by FeedItemCategoriesTable.category
}
