package eu.vitamo.app.ui.feed

import kotlin.uuid.Uuid

sealed interface FeedEvent {

    data object Refresh : FeedEvent

    data object LoadMore : FeedEvent

    data object Retry : FeedEvent

    data object RetryLoadMore : FeedEvent

    data class ToggleFeedItemLike(
        val feedItemId: Uuid,
    ) : FeedEvent

    data class ToggleReactionLike(
        val feedItemId: Uuid,
        val reactionId: Uuid,
    ) : FeedEvent

    data class OpenCommentComposer(
        val feedItemId: Uuid,
    ) : FeedEvent

    data class OpenReplyComposer(
        val feedItemId: Uuid,
        val commentId: Uuid,
    ) : FeedEvent

    data class ComposerTextChanged(
        val value: String,
    ) : FeedEvent

    data object SubmitComposer : FeedEvent

    data object DismissComposer : FeedEvent
}