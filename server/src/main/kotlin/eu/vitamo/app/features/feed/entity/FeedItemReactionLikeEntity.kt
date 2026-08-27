package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemReactionLikeTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemReactionLikeEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<FeedItemReactionLikeEntity>(
        FeedItemReactionLikeTable,
    )

    var reactionId by FeedItemReactionLikeTable.reactionId
    var userId by FeedItemReactionLikeTable.userId
    var createdAt by FeedItemReactionLikeTable.createdAt
}