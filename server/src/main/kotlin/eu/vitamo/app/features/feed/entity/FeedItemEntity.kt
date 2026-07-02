package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemsTable
import eu.vitamo.app.features.user.entity.UserEntity
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<FeedItemEntity>(FeedItemsTable)

    var author by UserEntity referencedOn FeedItemsTable.author
    var contentJson by FeedItemsTable.contentJson
    var privacy by FeedItemsTable.privacy
    var createdAt by FeedItemsTable.createdAt
    var updatedAt by FeedItemsTable.updatedAt
    var deletedAt by FeedItemsTable.deletedAt
}
