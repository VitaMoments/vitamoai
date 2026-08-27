package eu.vitamo.app.features.feed.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemReactionLikeTable : UuidTable(
    name = "feed_item_reaction_likes",
) {
    val reactionId = reference(
        name = "reaction_id",
        foreign = FeedItemReactionTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_reaction_likes_reaction_id_idx")

    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_reaction_likes_user_id_idx")

    val createdAt = long(
        name = "created_at",
    )

    init {
        uniqueIndex(
            "feed_item_reaction_likes_reaction_user_uidx",
            reactionId,
            userId,
        )
    }
}