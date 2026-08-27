package eu.vitamo.app.features.feed.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemReactionTable : UuidTable(
    name = "feed_item_reactions",
) {
    val feedItemId = reference(
        name = "feed_item_id",
        foreign = FeedItemTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_reactions_feed_item_id_idx")

    val parentReactionId = uuid(
        name = "parent_reaction_id",
    ).nullable()
        .index("feed_item_reactions_parent_reaction_id_idx")

    val authorId = reference(
        name = "author_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_item_reactions_author_id_idx")

    val contentJson = text(
        name = "content_json",
    )

    val createdAt = long(
        name = "created_at",
    )

    val updatedAt = long(
        name = "updated_at",
    )

    val deletedAt = long(
        name = "deleted_at",
    ).nullable()
}