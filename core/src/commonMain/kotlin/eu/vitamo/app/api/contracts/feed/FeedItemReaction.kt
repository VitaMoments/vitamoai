package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.api.result.PagedResult
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface FeedItemReaction {
    val id: Uuid
    val author: User
    val content: RichTextDocument
    val likes: PagedResult<User>
    val interactionContext: FeedItemInteractionContext
    val replies: List<FeedItemReaction>
        get() = emptyList()
}