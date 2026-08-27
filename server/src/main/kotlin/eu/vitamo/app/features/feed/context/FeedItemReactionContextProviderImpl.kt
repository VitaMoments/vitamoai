package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.api.contracts.feed.FeedItemInteractionPermissions
import eu.vitamo.app.api.contracts.feed.FeedItemPermission
import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FeedItemReactionContextProviderImpl(
    private val reactionRepository: FeedItemReactionRepository,
) : FeedItemReactionContextProvider {

    override suspend fun resolve(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
    ): FeedItemInteractionContext {
        val likedByMe = when (
            val result = reactionRepository.hasLiked(
                reactionId = reaction.id,
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

        return createContext(
            currentUserId = currentUserId,
            reaction = reaction,
            likedByMe = likedByMe,
        )
    }

    override suspend fun resolveAll(
        currentUserId: Uuid,
        reactions: List<FeedItemReactionRecord>,
    ): Map<Uuid, FeedItemInteractionContext> {
        if (reactions.isEmpty()) {
            return emptyMap()
        }

        val likedReactionIds = when (
            val result = reactionRepository
                .findLikedReactionIds(
                    userId = currentUserId,
                    reactionIds = reactions.map(
                        FeedItemReactionRecord::id,
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

        return reactions.associate { reaction ->
            reaction.id to createContext(
                currentUserId = currentUserId,
                reaction = reaction,
                likedByMe =
                    reaction.id in likedReactionIds,
            )
        }
    }

    private fun createContext(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
        likedByMe: Boolean,
    ): FeedItemInteractionContext {
        val isOwner =
            currentUserId == reaction.authorId

        val isComment =
            reaction.parentReactionId == null

        return FeedItemInteractionContext(
            likedByMe = likedByMe,
            permissions = FeedItemInteractionPermissions(
                canReply = permission(
                    granted = isComment,
                ),
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