package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemLikeTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemLikeEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<FeedItemLikeEntity>(
        FeedItemLikeTable,
    )

    var feedItemId by FeedItemLikeTable.feedItemId
    var userId by FeedItemLikeTable.userId
    var createdAt by FeedItemLikeTable.createdAt
}