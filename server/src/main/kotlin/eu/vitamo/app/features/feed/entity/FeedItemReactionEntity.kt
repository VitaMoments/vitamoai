package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.FeedItemReactionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FeedItemReactionEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<FeedItemReactionEntity>(
        FeedItemReactionTable,
    )

    var feedItemId by FeedItemReactionTable.feedItemId
    var parentReactionId by FeedItemReactionTable.parentReactionId
    var authorId by FeedItemReactionTable.authorId

    var contentJson by FeedItemReactionTable.contentJson

    var createdAt by FeedItemReactionTable.createdAt
    var updatedAt by FeedItemReactionTable.updatedAt
    var deletedAt by FeedItemReactionTable.deletedAt

    val isComment: Boolean
        get() = parentReactionId == null

    val isReply: Boolean
        get() = parentReactionId != null
}