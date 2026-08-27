package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<FeedItemEntity>(FeedItemTable)

    var authorId by FeedItemTable.authorId
    var type by FeedItemTable.type

    var createdAt by FeedItemTable.createdAt
    var updatedAt by FeedItemTable.updatedAt
    var deletedAt by FeedItemTable.deletedAt
}