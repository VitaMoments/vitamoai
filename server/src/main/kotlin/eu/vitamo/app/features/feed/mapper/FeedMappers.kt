package eu.vitamo.app.features.feed.mapper

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.user.PublicUser
import eu.vitamo.app.features.feed.model.FeedItemRecord
import eu.vitamo.app.features.user.model.UserAccount

fun FeedItemRecord.toFeedItem(author: UserAccount): FeedItem {
    return FeedItem(
        uuid = uuid,
        author = PublicUser(
            id = author.id,
            displayName = author.displayName,
            bio = author.bio,
            role = author.role,
        ),
        content = content,
        privacy = privacy,
        categories = categories,
        mediaAssets = mediaAssets,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
