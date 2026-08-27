package eu.vitamo.app.features.feed.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemLikeTable : UuidTable(
    name = "feed_item_likes",
) {
    val feedItemId = reference(
        name = "feed_item_id",
        foreign = FeedItemTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_likes_feed_item_id_idx")

    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_likes_user_id_idx")

    val createdAt = long(
        name = "created_at",
    )

    init {
        uniqueIndex(
            "feed_item_likes_feed_item_user_uidx",
            feedItemId,
            userId,
        )
    }
}