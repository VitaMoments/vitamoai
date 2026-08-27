package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.FeedItemComment
import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.api.contracts.feed.FeedItemReaction
import eu.vitamo.app.api.contracts.feed.FeedItemReply
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class FeedItemReactionContextLoader(
    private val reactionRepository: FeedItemReactionRepository,
    private val contextProvider: FeedItemReactionContextProvider,
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
) {

    suspend fun load(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
    ): FeedItemReaction? {
        val usersById = loadUsersById(
            userIds = setOf(
                reaction.authorId,
            ),
        )

        val interactionContext =
            contextProvider.resolve(
                currentUserId = currentUserId,
                reaction = reaction,
            )

        return if (reaction.isComment) {
            loadComment(
                currentUserId = currentUserId,
                reaction = reaction,
                usersById = usersById,
                interactionContext =
                    interactionContext,
            )
        } else {
            loadReply(
                currentUserId = currentUserId,
                reaction = reaction,
                usersById = usersById,
                interactionContext =
                    interactionContext,
            )
        }
    }

    suspend fun loadAll(
        currentUserId: Uuid,
        reactions: List<FeedItemReactionRecord>,
    ): List<FeedItemReaction> {
        if (reactions.isEmpty()) {
            return emptyList()
        }

        val usersById = loadUsersById(
            userIds = reactions
                .mapTo(
                    mutableSetOf(),
                ) {
                    it.authorId
                },
        )

        val contextsByReactionId =
            contextProvider.resolveAll(
                currentUserId = currentUserId,
                reactions = reactions,
            )

        return reactions.mapNotNull { reaction ->
            val interactionContext =
                contextsByReactionId[
                    reaction.id
                ]
                    ?: return@mapNotNull null

            if (reaction.isComment) {
                loadComment(
                    currentUserId = currentUserId,
                    reaction = reaction,
                    usersById = usersById,
                    interactionContext =
                        interactionContext,
                )
            } else {
                loadReply(
                    currentUserId = currentUserId,
                    reaction = reaction,
                    usersById = usersById,
                    interactionContext =
                        interactionContext,
                )
            }
        }
    }

    suspend fun loadCommentsPage(
        currentUserId: Uuid,
        page: Page<FeedItemReactionRecord>,
    ): PagedResult<FeedItemComment> {
        val comments = loadAll(
            currentUserId = currentUserId,
            reactions = page.items,
        )
            .filterIsInstance<FeedItemComment>()

        return page.toPagedResult(
            items = comments,
        )
    }

    suspend fun loadRepliesPage(
        currentUserId: Uuid,
        page: Page<FeedItemReactionRecord>,
    ): PagedResult<FeedItemReply> {
        val replies = loadAll(
            currentUserId = currentUserId,
            reactions = page.items,
        )
            .filterIsInstance<FeedItemReply>()

        return page.toPagedResult(
            items = replies,
        )
    }

    private suspend fun loadComment(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
        usersById: Map<Uuid, UserRecord>,
        interactionContext:
        FeedItemInteractionContext,
    ): FeedItemComment? {
        val authorRecord =
            usersById[reaction.authorId]
                ?: return null

        val author = userContextLoader
            .load(
                currentUserId = currentUserId,
                targetUser = authorRecord,
            )
            .user

        val likes = loadLikes(
            currentUserId = currentUserId,
            reactionId = reaction.id,
        )

        val replies = loadReplies(
            currentUserId = currentUserId,
            comment = reaction,
        )

        return FeedItemComment(
            id = reaction.id,
            author = author,
            content = decodeContent(
                contentJson =
                    reaction.contentJson,
            ),
            likes = likes,
            interactionContext =
                interactionContext,
            pagedReplies = replies,
        )
    }

    private suspend fun loadReply(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
        usersById: Map<Uuid, UserRecord>,
        interactionContext:
        FeedItemInteractionContext,
    ): FeedItemReply? {
        val authorRecord =
            usersById[reaction.authorId]
                ?: return null

        val author = userContextLoader
            .load(
                currentUserId = currentUserId,
                targetUser = authorRecord,
            )
            .user

        val likes = loadLikes(
            currentUserId = currentUserId,
            reactionId = reaction.id,
        )

        return FeedItemReply(
            id = reaction.id,
            author = author,
            content = decodeContent(
                contentJson =
                    reaction.contentJson,
            ),
            likes = likes,
            interactionContext =
                interactionContext,
        )
    }

    private suspend fun loadReplies(
        currentUserId: Uuid,
        comment: FeedItemReactionRecord,
    ): PagedResult<FeedItemReply> {
        val result =
            reactionRepository.findByFeedItemId(
                feedItemId =
                    comment.feedItemId,
                parentReactionId =
                    comment.id,
                limit =
                    DEFAULT_REPLIES_LIMIT,
                offset = 0L,
            )

        return when (result) {
            is RepositoryResult.Success -> {
                loadRepliesPage(
                    currentUserId =
                        currentUserId,
                    page = result.data,
                )
            }

            is RepositoryResult.Error -> {
                emptyPagedResult(
                    limit =
                        DEFAULT_REPLIES_LIMIT,
                )
            }
        }
    }

    private suspend fun loadLikes(
        currentUserId: Uuid,
        reactionId: Uuid,
    ): PagedResult<User> {
        return when (
            val result =
                reactionRepository
                    .findLikeUserIds(
                        reactionId =
                            reactionId,
                        limit =
                            DEFAULT_LIKES_LIMIT,
                        offset = 0L,
                    )
        ) {
            is RepositoryResult.Success -> {
                val page =
                    result.data

                if (page.items.isEmpty()) {
                    return page.toPagedResult(
                        items = emptyList(),
                    )
                }

                val usersById =
                    loadUsersById(
                        userIds =
                            page.items,
                    )

                val orderedRecords =
                    page.items
                        .mapNotNull { userId ->
                            usersById[userId]
                        }

                val users =
                    userContextLoader
                        .loadAll(
                            currentUserId =
                                currentUserId,
                            targetUsers =
                                orderedRecords,
                        )
                        .map {
                            it.user
                        }

                page.toPagedResult(
                    items = users,
                )
            }

            is RepositoryResult.Error -> {
                emptyPagedResult(
                    limit =
                        DEFAULT_LIKES_LIMIT,
                )
            }
        }
    }

    private suspend fun loadUsersById(
        userIds: Collection<Uuid>,
    ): Map<Uuid, UserRecord> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }

        return when (
            val result =
                userRepository.findByIds(
                    ids = userIds,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data.associateBy(
                    UserRecord::id,
                )
            }

            is RepositoryResult.Error -> {
                emptyMap()
            }
        }
    }

    private fun decodeContent(
        contentJson: String,
    ): RichTextDocument =
        Json.decodeFromString<RichTextDocument>(
            contentJson,
        )

    private fun <T> Page<*>.toPagedResult(
        items: List<T>,
    ): PagedResult<T> {
        val nextOffsetValue =
            offset +
                    this.items.size.toLong()

        val hasMore =
            nextOffsetValue < total

        return PagedResult(
            items = items,
            limit = limit,
            offset = offset,
            total = total,
            hasMore = hasMore,
            nextOffset = if (hasMore) {
                nextOffsetValue
            } else {
                null
            },
        )
    }

    private fun <T> emptyPagedResult(
        limit: Int,
    ): PagedResult<T> =
        PagedResult(
            items = emptyList(),
            limit = limit,
            offset = 0L,
            total = 0L,
            hasMore = false,
            nextOffset = null,
        )

    private companion object {
        const val DEFAULT_LIKES_LIMIT = 20
        const val DEFAULT_REPLIES_LIMIT = 20
    }
}