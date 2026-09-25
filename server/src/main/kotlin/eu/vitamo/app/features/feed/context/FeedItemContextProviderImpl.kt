package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.api.contracts.feed.FeedItemInteractionPermissions
import eu.vitamo.app.api.contracts.feed.FeedItemPermission
import eu.vitamo.app.api.contracts.user.capabilities.UserAction
import eu.vitamo.app.features.feed.model.FeedItemRecord
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.features.user.context.UserCapabilitiesProvider
import eu.vitamo.app.features.user.model.UserSettingsRecord
import eu.vitamo.app.features.user.repository.UserSettingsRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FeedItemContextProviderImpl(
    private val feedItemRepository: FeedItemRepository,
    private val userCapabilitiesProvider: UserCapabilitiesProvider,
    private val userSettingsRepository: UserSettingsRepository,
) : FeedItemContextProvider {

    override suspend fun resolve(
        currentUserId: Uuid,
        feedItem: FeedItemRecord,
    ): FeedItemInteractionContext {
        val likedByMe =
            resolveLikedByMe(
                currentUserId = currentUserId,
                feedItemId = feedItem.id,
            )

        val canCreateComment =
            resolveCanCreateComment(
                currentUserId = currentUserId,
            )

        val authorCommentsEnabled =
            resolveAuthorCommentsEnabled(
                authorId = feedItem.authorId,
            )

        return createContext(
            currentUserId = currentUserId,
            feedItem = feedItem,
            likedByMe = likedByMe,
            canCreateComment = canCreateComment,
            authorCommentsEnabled = authorCommentsEnabled,
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
            resolveLikedFeedItemIds(
                currentUserId = currentUserId,
                feedItems = feedItems,
            )

        /*
         * De capabilities zijn voor alle feeditems hetzelfde,
         * want het gaat steeds om dezelfde current user.
         */
        val canCreateComment =
            resolveCanCreateComment(
                currentUserId = currentUserId,
            )

        /*
         * Settings van alle auteurs in één query ophalen.
         */
        val authorSettings =
            resolveAuthorSettings(
                authorIds = feedItems
                    .map(FeedItemRecord::authorId)
                    .distinct(),
            )

        return feedItems.associate { feedItem ->
            val settings =
                authorSettings[feedItem.authorId]

            feedItem.id to createContext(
                currentUserId = currentUserId,
                feedItem = feedItem,
                likedByMe =
                    feedItem.id in likedFeedItemIds,
                canCreateComment =
                    canCreateComment,
                authorCommentsEnabled =
                    settings?.commentsEnabled
                        ?: false,
            )
        }
    }

    private fun createContext(
        currentUserId: Uuid,
        feedItem: FeedItemRecord,
        likedByMe: Boolean,
        canCreateComment: Boolean,
        authorCommentsEnabled: Boolean,
    ): FeedItemInteractionContext {
        val isOwner =
            currentUserId == feedItem.authorId

        val canReply =
            canCreateComment &&
                    authorCommentsEnabled &&
                    feedItem.commentsEnabled

        return FeedItemInteractionContext(
            likedByMe = likedByMe,
            permissions = FeedItemInteractionPermissions(
                canReply = permission(
                    granted = canReply,
                ),
                canLike =
                    FeedItemPermission.GRANTED,
                canEdit = permission(
                    granted = isOwner,
                ),
                canDelete = permission(
                    granted = isOwner,
                ),
            ),
        )
    }

    private suspend fun resolveCanCreateComment(
        currentUserId: Uuid,
    ): Boolean =
        when (
            val result =
                userCapabilitiesProvider.resolve(
                    userId = currentUserId,
                )
        ) {
            is RepositoryResult.Success -> {
                UserAction.COMMENT_CREATE in
                        result.data.actions
            }

            is RepositoryResult.Error -> {
                false
            }
        }

    private suspend fun resolveAuthorCommentsEnabled(
        authorId: Uuid,
    ): Boolean =
        when (
            val result =
                userSettingsRepository.findByUserId(
                    userId = authorId,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data.commentsEnabled
            }

            is RepositoryResult.Error -> {
                false
            }
        }

    private suspend fun resolveAuthorSettings(
        authorIds: Collection<Uuid>,
    ): Map<Uuid, UserSettingsRecord> =
        when (
            val result =
                userSettingsRepository.findByUserIds(
                    userIds = authorIds,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                emptyMap()
            }
        }

    private suspend fun resolveLikedFeedItemIds(
        currentUserId: Uuid,
        feedItems: List<FeedItemRecord>,
    ): Set<Uuid> =
        when (
            val result =
                feedItemRepository.findLikedFeedItemIds(
                    userId = currentUserId,
                    feedItemIds = feedItems.map(
                        FeedItemRecord::id,
                    ),
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                emptySet()
            }
        }

    private suspend fun resolveLikedByMe(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): Boolean =
        when (
            val result =
                feedItemRepository.hasLiked(
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

    private fun permission(
        granted: Boolean,
    ): FeedItemPermission =
        if (granted) {
            FeedItemPermission.GRANTED
        } else {
            FeedItemPermission.DENIED
        }
}