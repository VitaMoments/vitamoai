package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.api.contracts.feed.FeedItemInteractionPermissions
import eu.vitamo.app.api.contracts.feed.FeedItemPermission
import eu.vitamo.app.features.feed.model.FeedItemRecord
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FeedItemContextProviderImpl(
    private val feedItemRepository: FeedItemRepository,
) : FeedItemContextProvider {

    override suspend fun resolve(
        currentUserId: Uuid,
        feedItem: FeedItemRecord,
    ): FeedItemInteractionContext {
        val likedByMe = resolveLikedByMe(
            currentUserId = currentUserId,
            feedItemId = feedItem.id,
        )

        return createContext(
            currentUserId = currentUserId,
            feedItem = feedItem,
            likedByMe = likedByMe,
        )
    }

    override suspend fun resolveAll(
        currentUserId: Uuid,
        feedItems: List<FeedItemRecord>,
    ): Map<Uuid, FeedItemInteractionContext> {
        if (feedItems.isEmpty()) {
            return emptyMap()
        }

        val likedFeedItemIds =
            when (
                val result =
                    feedItemRepository.findLikedFeedItemIds(
                        userId = currentUserId,
                        feedItemIds = feedItems.map(
                            FeedItemRecord::id,
                        ),
                    )
            ) {
                is RepositoryResult.Success ->
                    result.data

                is RepositoryResult.Error ->
                    emptySet()
            }

        return feedItems.associate { feedItem ->
            feedItem.id to createContext(
                currentUserId = currentUserId,
                feedItem = feedItem,
                likedByMe =
                    feedItem.id in likedFeedItemIds,
            )
        }
    }

    private suspend fun resolveLikedByMe(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): Boolean =
        when (
            val result = feedItemRepository.hasLiked(
                feedItemId = feedItemId,
                userId = currentUserId,
            )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                false
            }
        }

    private fun createContext(
        currentUserId: Uuid,
        feedItem: FeedItemRecord,
        likedByMe: Boolean,
    ): FeedItemInteractionContext {
        val isOwner =
            currentUserId == feedItem.authorId

        return FeedItemInteractionContext(
            likedByMe = likedByMe,
            permissions = FeedItemInteractionPermissions(
                canReply = FeedItemPermission.GRANTED,
                canLike = FeedItemPermission.GRANTED,
                canEdit = permission(
                    granted = isOwner,
                ),
                canDelete = permission(
                    granted = isOwner,
                ),
            ),
        )
    }

    private fun permission(
        granted: Boolean,
    ): FeedItemPermission =
        if (granted) {
            FeedItemPermission.GRANTED
        } else {
            FeedItemPermission.DENIED
        }
}