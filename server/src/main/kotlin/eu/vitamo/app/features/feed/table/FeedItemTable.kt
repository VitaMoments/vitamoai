package eu.vitamo.app.features.feed.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object FeedItemTable : UuidTable(
    name = "feed_items",
) {
    val authorId = reference(
        name = "author_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("feed_items_author_id_idx")

    val type = varchar(
        name = "type",
        length = 32,
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