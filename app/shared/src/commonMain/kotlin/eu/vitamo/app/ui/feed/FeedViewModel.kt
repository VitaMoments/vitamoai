package eu.vitamo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemReaction
import eu.vitamo.app.features.feed.usecase.GetFeedUseCase
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            FeedState(),
        )

    val state: StateFlow<FeedState> =
        _state.asStateFlow()

    private var loadJob: Job? =
        null

    init {
        loadInitial()
    }

    fun onEvent(
        event: FeedEvent,
    ) {
        when (event) {
            FeedEvent.Refresh -> {
                refresh()
            }

            FeedEvent.LoadMore -> {
                loadMore()
            }

            FeedEvent.Retry -> {
                loadInitial()
            }

            FeedEvent.RetryLoadMore -> {
                retryLoadMore()
            }

            is FeedEvent.ToggleFeedItemLike -> {
                toggleFeedItemLike(
                    feedItemId =
                        event.feedItemId,
                )
            }

            is FeedEvent.ToggleReactionLike -> {
                toggleReactionLike(
                    feedItemId =
                        event.feedItemId,
                    reactionId =
                        event.reactionId,
                )
            }

            is FeedEvent.OpenCommentComposer -> {
                openCommentComposer(
                    feedItemId =
                        event.feedItemId,
                )
            }

            is FeedEvent.OpenReplyComposer -> {
                openReplyComposer(
                    feedItemId =
                        event.feedItemId,
                    commentId =
                        event.commentId,
                )
            }

            is FeedEvent.ComposerTextChanged -> {
                updateComposerText(
                    value =
                        event.value,
                )
            }

            FeedEvent.SubmitComposer -> {
                submitComposer()
            }

            FeedEvent.DismissComposer -> {
                dismissComposer()
            }
        }
    }

    private fun loadInitial() {
        loadJob?.cancel()

        loadJob =
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isInitialLoading = true,
                        isRefreshing = false,
                        isLoadingMore = false,
                        initialError = null,
                        loadMoreError = null,
                        nextOffset = 0L,
                    )
                }

                when (
                    val result =
                        getFeedUseCase(
                            limit = PAGE_SIZE,
                            offset = 0L,
                        )
                ) {
                    is RepositoryResult.Success -> {
                        val page =
                            result.data

                        _state.update {
                            it.copy(
                                items =
                                    page.items,
                                isInitialLoading =
                                    false,
                                isRefreshing =
                                    false,
                                isLoadingMore =
                                    false,
                                nextOffset =
                                    page.nextOffset,
                                initialError =
                                    null,
                                loadMoreError =
                                    null,
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _state.update {
                            it.copy(
                                items =
                                    emptyList(),
                                isInitialLoading =
                                    false,
                                isRefreshing =
                                    false,
                                isLoadingMore =
                                    false,
                                nextOffset =
                                    null,
                                initialError =
                                    result.error.message,
                            )
                        }
                    }
                }
            }
    }

    private fun refresh() {
        val currentState =
            state.value

        if (
            currentState.isRefreshing ||
            currentState.isInitialLoading
        ) {
            return
        }

        loadJob?.cancel()

        loadJob =
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isRefreshing = true,
                        loadMoreError = null,
                    )
                }

                try {
                    when (
                        val result =
                            getFeedUseCase(
                                limit = PAGE_SIZE,
                                offset = 0L,
                            )
                    ) {
                        is RepositoryResult.Success -> {
                            val page =
                                result.data

                            _state.update {
                                it.copy(
                                    items =
                                        page.items,
                                    isRefreshing =
                                        false,
                                    nextOffset =
                                        page.nextOffset,
                                    initialError =
                                        null,
                                    loadMoreError =
                                        null,
                                )
                            }
                        }

                        is RepositoryResult.Error -> {
                            _state.update {
                                it.copy(
                                    isRefreshing =
                                        false,
                                )
                            }
                        }
                    }
                } catch (
                    cause: CancellationException,
                ) {
                    throw cause
                } catch (
                    cause: Throwable,
                ) {
                    _state.update {
                        it.copy(
                            isRefreshing =
                                false,
                        )
                    }
                }
            }
    }

    private fun loadMore() {
        val currentState =
            state.value

        val nextOffset =
            currentState.nextOffset
                ?: return

        if (
            currentState.isInitialLoading ||
            currentState.isRefreshing ||
            currentState.isLoadingMore
        ) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingMore = true,
                    loadMoreError = null,
                )
            }

            try {
                when (
                    val result =
                        getFeedUseCase(
                            limit = PAGE_SIZE,
                            offset = nextOffset,
                        )
                ) {
                    is RepositoryResult.Success -> {
                        val page =
                            result.data

                        _state.update { currentState ->
                            val existingIds =
                                currentState.items
                                    .mapTo(
                                        mutableSetOf(),
                                    ) {
                                        it.id
                                    }

                            val newItems =
                                page.items.filter {
                                    it.id !in existingIds
                                }

                            currentState.copy(
                                items =
                                    currentState.items +
                                            newItems,
                                isLoadingMore =
                                    false,
                                nextOffset =
                                    page.nextOffset,
                                loadMoreError =
                                    null,
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoadingMore =
                                    false,
                                loadMoreError =
                                    result.error.message,
                            )
                        }
                    }
                }
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } catch (
                cause: Throwable,
            ) {
                _state.update {
                    it.copy(
                        isLoadingMore =
                            false,
                        loadMoreError =
                            cause.message
                                ?: "Meer berichten konden niet worden geladen.",
                    )
                }
            }
        }
    }

    private fun retryLoadMore() {
        _state.update {
            it.copy(
                loadMoreError = null,
            )
        }

        loadMore()
    }

    private fun toggleFeedItemLike(
        feedItemId: Uuid,
    ) {
        val currentState =
            state.value

        if (
            currentState.isFeedItemLikePending(
                feedItemId = feedItemId,
            )
        ) {
            return
        }

        val feedItem =
            currentState.items
                .firstOrNull {
                    it.id == feedItemId
                }
                ?: return

        if (!feedItem.canLike) {
            return
        }

        val currentlyLiked =
            feedItem.interactionContext
                .likedByMe

        viewModelScope.launch {
            setFeedItemLikePending(
                feedItemId = feedItemId,
                pending = true,
            )

            try {
//                val result =
//                    if (currentlyLiked) {
//                        unlikeFeedItemUseCase(
//                            feedItemId =
//                                feedItemId,
//                        )
//                    } else {
//                        likeFeedItemUseCase(
//                            feedItemId =
//                                feedItemId,
//                        )
//                    }

//                when (result) {
//                    is RepositoryResult.Success -> {
//                        /*
//                         * Server opnieuw ophalen zodat:
//                         *
//                         * - likedByMe
//                         * - totalLikes
//                         * - permissions
//                         *
//                         * weer volledig server-authoritative zijn.
//                         */
//                        refreshAfterMutation()
//                    }
//
//                    is RepositoryResult.Error -> {
//                        // Voor nu state behouden.
//                        // Later eventueel FeedEffect.ShowMessage toevoegen.
//                    }
//                }
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } finally {
                setFeedItemLikePending(
                    feedItemId = feedItemId,
                    pending = false,
                )
            }
        }
    }

    private fun toggleReactionLike(
        feedItemId: Uuid,
        reactionId: Uuid,
    ) {
        val currentState =
            state.value

        if (
            currentState.isReactionLikePending(
                reactionId = reactionId,
            )
        ) {
            return
        }

        val feedItem =
            currentState.items
                .firstOrNull {
                    it.id == feedItemId
                }
                ?: return

        val reaction =
            findReaction(
                feedItem = feedItem,
                reactionId = reactionId,
            ) ?: return

        val canLike =
            reaction.interactionContext
                .permissions
                .canLike
                .isGranted()

        if (!canLike) {
            return
        }

        val currentlyLiked =
            reaction.interactionContext
                .likedByMe

        viewModelScope.launch {
            setReactionLikePending(
                reactionId = reactionId,
                pending = true,
            )

            try {
//                val result =
//                    if (currentlyLiked) {
//                        unlikeFeedItemReactionUseCase(
//                            reactionId =
//                                reactionId,
//                        )
//                    } else {
//                        likeFeedItemReactionUseCase(
//                            reactionId =
//                                reactionId,
//                        )
//                    }

//                when (result) {
//                    is RepositoryResult.Success -> {
//                        refreshAfterMutation()
//                    }
//
//                    is RepositoryResult.Error -> {
//                        // Later eventueel snackbar/effect.
//                    }
//                }
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } finally {
                setReactionLikePending(
                    reactionId = reactionId,
                    pending = false,
                )
            }
        }
    }

    private fun openCommentComposer(
        feedItemId: Uuid,
    ) {
        val feedItem =
            state.value.items
                .firstOrNull {
                    it.id == feedItemId
                }
                ?: return

        /*
         * De app vertrouwt voor UX op de
         * interaction context van de server.
         */
        if (!feedItem.canReply) {
            return
        }

        _state.update {
            it.copy(
                composerTarget =
                    FeedComposerTarget.Comment(
                        feedItemId =
                            feedItemId,
                    ),
                composerText = "",
            )
        }
    }

    private fun openReplyComposer(
        feedItemId: Uuid,
        commentId: Uuid,
    ) {
        val feedItem =
            state.value.items
                .firstOrNull {
                    it.id == feedItemId
                }
                ?: return

        val comment =
            findReaction(
                feedItem = feedItem,
                reactionId = commentId,
            ) ?: return

        val canReply =
            comment.interactionContext
                .permissions
                .canReply
                .isGranted()

        if (!canReply) {
            return
        }

        _state.update {
            it.copy(
                composerTarget =
                    FeedComposerTarget.Reply(
                        feedItemId =
                            feedItemId,
                        commentId =
                            commentId,
                    ),
                composerText = "",
            )
        }
    }

    private fun updateComposerText(
        value: String,
    ) {
        if (
            state.value.isSubmittingReaction
        ) {
            return
        }

        _state.update {
            it.copy(
                composerText =
                    value.take(
                        MAX_COMMENT_LENGTH,
                    ),
            )
        }
    }

    private fun dismissComposer() {
        if (
            state.value.isSubmittingReaction
        ) {
            return
        }

        _state.update {
            it.copy(
                composerTarget = null,
                composerText = "",
            )
        }
    }

    private fun submitComposer() {
        val currentState =
            state.value

        val target =
            currentState.composerTarget
                ?: return

        if (
            currentState.isSubmittingReaction
        ) {
            return
        }

        val text =
            currentState.composerText
                .trim()

        if (text.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmittingReaction =
                        true,
                )
            }

            try {
                val result =
                    when (target) {
                        is FeedComposerTarget.Comment -> {
//                            createFeedItemCommentUseCase(
//                                feedItemId =
//                                    target.feedItemId,
//                                text =
//                                    text,
//                            )
                        }

                        is FeedComposerTarget.Reply -> {
//                            createFeedItemReplyUseCase(
//                                feedItemId =
//                                    target.feedItemId,
//                                parentReactionId =
//                                    target.commentId,
//                                text =
//                                    text,
//                            )
                        }
                    }

//                when (result) {
//                    is RepositoryResult.Success -> {
//                        _state.update {
//                            it.copy(
//                                composerTarget =
//                                    null,
//                                composerText =
//                                    "",
//                                isSubmittingReaction =
//                                    false,
//                            )
//                        }
//
//                        refreshAfterMutation()
//                    }
//
//                    is RepositoryResult.Error -> {
//                        _state.update {
//                            it.copy(
//                                isSubmittingReaction =
//                                    false,
//                            )
//                        }
//                    }
//                }
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } catch (
                cause: Throwable,
            ) {
                _state.update {
                    it.copy(
                        isSubmittingReaction =
                            false,
                    )
                }
            }
        }
    }

    /**
     * Na een mutatie halen we voorlopig de eerste pagina
     * opnieuw op.
     *
     * Dat is iets minder efficiënt dan een optimistic
     * lokale mutation, maar ideaal om nu de serverlogica,
     * totals en interaction-context te testen.
     */
    private suspend fun refreshAfterMutation() {
        when (
            val result =
                getFeedUseCase(
                    limit = PAGE_SIZE,
                    offset = 0L,
                )
        ) {
            is RepositoryResult.Success -> {
                val page =
                    result.data

                _state.update {
                    it.copy(
                        items =
                            page.items,
                        nextOffset =
                            page.nextOffset,
                        initialError =
                            null,
                    )
                }
            }

            is RepositoryResult.Error -> {
                /*
                 * De oorspronkelijke feed blijft staan
                 * wanneer refresh na een mutation faalt.
                 */
            }
        }
    }

    private fun setFeedItemLikePending(
        feedItemId: Uuid,
        pending: Boolean,
    ) {
        _state.update { currentState ->
            currentState.copy(
                pendingFeedItemLikeIds =
                    if (pending) {
                        currentState
                            .pendingFeedItemLikeIds +
                                feedItemId
                    } else {
                        currentState
                            .pendingFeedItemLikeIds -
                                feedItemId
                    },
            )
        }
    }

    private fun setReactionLikePending(
        reactionId: Uuid,
        pending: Boolean,
    ) {
        _state.update { currentState ->
            currentState.copy(
                pendingReactionLikeIds =
                    if (pending) {
                        currentState
                            .pendingReactionLikeIds +
                                reactionId
                    } else {
                        currentState
                            .pendingReactionLikeIds -
                                reactionId
                    },
            )
        }
    }

    private fun findReaction(
        feedItem: FeedItem,
        reactionId: Uuid,
    ): FeedItemReaction? {
        feedItem.reactions.items
            .forEach { comment ->
                if (comment.id == reactionId) {
                    return comment
                }

                comment.replies
                    .firstOrNull {
                        it.id == reactionId
                    }
                    ?.let {
                        return it
                    }
            }

        return null
    }

    private fun eu.vitamo.app.api.contracts.feed.FeedItemPermission
            .isGranted(): Boolean =
        this ==
                eu.vitamo.app.api.contracts.feed.FeedItemPermission.GRANTED

    private companion object {
        const val PAGE_SIZE =
            20

        const val MAX_COMMENT_LENGTH =
            2_000
    }
}