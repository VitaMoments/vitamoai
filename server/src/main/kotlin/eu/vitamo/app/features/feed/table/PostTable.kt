package eu.vitamo.app.features.feed.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object PostTable : UuidTable(
    name = "posts",
) {
    val feedItemId = reference(
        name = "feed_item_id",
        foreign = FeedItemTable,
        onDelete = ReferenceOption.CASCADE,
    ).uniqueIndex("posts_feed_item_id_uidx")

    val title = varchar(
        name = "title",
        length = 255,
    ).nullable()

    val messageJson = text(
        name = "message_json",
    ).nullable()
}