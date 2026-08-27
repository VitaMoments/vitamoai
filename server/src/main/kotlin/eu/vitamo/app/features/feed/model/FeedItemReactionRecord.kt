package eu.vitamo.app.features.feed.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class FeedItemReactionRecord(
    val id: Uuid,
    val feedItemId: Uuid,
    val parentReactionId: Uuid?,
    val authorId: Uuid,
    val contentJson: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
) {
    val isComment: Boolean
        get() = parentReactionId == null

    val isReply: Boolean
        get() = parentReactionId != null
}