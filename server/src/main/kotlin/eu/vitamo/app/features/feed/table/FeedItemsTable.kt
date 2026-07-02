package eu.vitamo.app.features.feed.table

import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object FeedItemsTable : UuidTable(name = "feed_items") {
    val author = reference(
        name = "author_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val contentJson = text(name = "content_json")
    val privacy = enumerationByName(name = "privacy", length = 32, klass = PrivacyStatus::class)
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")
    val deletedAt = timestamp(name = "deleted_at").nullable()

    init {
        index(
            customIndexName = "idx_feed_items_author_created_at",
            isUnique = false,
            columns = arrayOf(author, createdAt),
        )
        index(
            customIndexName = "idx_feed_items_author_deleted_at",
            isUnique = false,
            columns = arrayOf(author, deletedAt),
        )
    }
}
