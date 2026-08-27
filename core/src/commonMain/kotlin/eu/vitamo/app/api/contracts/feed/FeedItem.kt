package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.api.result.PagedResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed class FeedItem {
    abstract val metaData: FeedItemMetaData
    abstract val content: FeedItemContent
    abstract val likes: PagedResult<User>
    abstract val reactions: PagedResult<FeedItemComment>
    abstract val interactionContext: FeedItemInteractionContext

    val id: Uuid
        get() = metaData.feedId
    val totalLikes: Long
        get() = likes.total
    val totalComments: Long
        get() = reactions.total
    val canLike: Boolean
        get() = interactionContext.permissions.canLike == FeedItemPermission.GRANTED
    val canReply: Boolean
        get() = interactionContext.permissions.canReply == FeedItemPermission.GRANTED
}

@Serializable
@SerialName("POST")
data class Post(
    override val metaData: FeedItemMetaData,
    override val content: FeedItemContent,
    override val likes: PagedResult<User>,
    override val reactions: PagedResult<FeedItemComment>,
    override val interactionContext: FeedItemInteractionContext
    ): FeedItem() {
    init { require(content.assets.isNotEmpty()) }
}