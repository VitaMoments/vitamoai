package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.api.result.PagedResult
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class FeedItemComment(
    override val id: Uuid,
    override val author: User,
    override val content: RichTextDocument,
    override val likes: PagedResult<User>,
    override val interactionContext: FeedItemInteractionContext,
    val pagedReplies: PagedResult<FeedItemReply>,
) : FeedItemReaction {
    override val replies: List<FeedItemReply>
        get() = pagedReplies.items
}
