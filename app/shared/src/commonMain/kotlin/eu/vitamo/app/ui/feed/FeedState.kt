package eu.vitamo.app.ui.feed

import eu.vitamo.app.api.contracts.feed.FeedItem
import kotlin.uuid.Uuid

data class FeedState(
    val items: List<FeedItem> = emptyList(),

    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,

    val nextOffset: Long? = 0L,

    val initialError: String? = null,
    val loadMoreError: String? = null,

    val pendingFeedItemLikeIds: Set<Uuid> =
        emptySet(),

    val pendingReactionLikeIds: Set<Uuid> =
        emptySet(),

    val composerTarget: FeedComposerTarget? =
        null,

    val composerText: String =
        "",

    val isSubmittingReaction: Boolean =
        false,
) {

    val isEmpty: Boolean
        get() =
            !isInitialLoading &&
                    initialError == null &&
                    items.isEmpty()

    fun isFeedItemLikePending(
        feedItemId: Uuid,
    ): Boolean =
        feedItemId in pendingFeedItemLikeIds

    fun isReactionLikePending(
        reactionId: Uuid,
    ): Boolean =
        reactionId in pendingReactionLikeIds
}