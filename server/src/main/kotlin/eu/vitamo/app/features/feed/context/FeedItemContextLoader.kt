package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemComment
import eu.vitamo.app.api.contracts.feed.FeedItemContent
import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.api.contracts.feed.FeedItemMetaData
import eu.vitamo.app.api.contracts.feed.Post
import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.feed.model.PostRecord
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.features.media.mapper.toContract
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class FeedItemContextLoader(
    private val feedItemRepository: FeedItemRepository,
    private val reactionRepository: FeedItemReactionRepository,
    private val mediaAssetRepository: MediaAssetRepository,
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
    private val reactionContextLoader: FeedItemReactionContextLoader,
    private val contextProvider: FeedItemContextProvider,
) {

    suspend fun load(
        currentUserId: Uuid,
        feedItem: PostRecord,
    ): Post? {
        val authorRecord = loadUsersById(
            userIds = setOf(
                feedItem.authorId,
            ),
        )[feedItem.authorId]
            ?: return null

        val interactionContext =
            contextProvider.resolve(
                currentUserId = currentUserId,
                feedItem = feedItem,
            )

        return loadPost(
            currentUserId = currentUserId,
            feedItem = feedItem,
            authorRecord = authorRecord,
            interactionContext = interactionContext,
        )
    }

    suspend fun loadAll(
        currentUserId: Uuid,
        feedItems: List<PostRecord>,
    ): List<Post> {
        if (feedItems.isEmpty()) {
            return emptyList()
        }

        val usersById = loadUsersById(
            userIds = feedItems
                .mapTo(
                    mutableSetOf(),
                ) {
                    it.authorId
                },
        )

        val contextsByFeedItemId =
            contextProvider.resolveAll(
                currentUserId = currentUserId,
                feedItems = feedItems,
            )

        return feedItems.mapNotNull { feedItem ->
            val authorRecord =
                usersById[feedItem.authorId]
                    ?: return@mapNotNull null

            val interactionContext =
                contextsByFeedItemId[feedItem.id]
                    ?: return@mapNotNull null

            loadPost(
                currentUserId = currentUserId,
                feedItem = feedItem,
                authorRecord = authorRecord,
                interactionContext = interactionContext,
            )
        }
    }

    suspend fun loadPage(
        currentUserId: Uuid,
        page: Page<PostRecord>,
    ): PagedResult<FeedItem> {
        val feedItems: List<FeedItem> =
            loadAll(
                currentUserId = currentUserId,
                feedItems = page.items,
            )

        return page.toPagedResult(
            items = feedItems,
        )
    }

    private suspend fun loadPost(
        currentUserId: Uuid,
        feedItem: PostRecord,
        authorRecord: UserRecord,
        interactionContext: FeedItemInteractionContext,
    ): Post? {
        val author = userContextLoader
            .load(
                currentUserId = currentUserId,
                targetUser = authorRecord,
            )
            .user

        val assets = loadAssets(
            feedItemId = feedItem.id,
        )

        /*
         * Een Post moet minimaal één geldig media-asset
         * bevatten.
         */
        if (assets.isEmpty()) {
            return null
        }

        val likes = loadLikes(
            currentUserId = currentUserId,
            feedItemId = feedItem.id,
        )

        val reactions = loadReactions(
            currentUserId = currentUserId,
            feedItemId = feedItem.id,
        )

        return Post(
            metaData = FeedItemMetaData(
                feedId = feedItem.id,
                author = author,
                createdAt = feedItem.createdAt,
                updatedAt = feedItem.updatedAt,
            ),
            content = FeedItemContent(
                title = feedItem.title,
                message = decodeMessage(
                    messageJson = feedItem.messageJson,
                ),
                assets = assets,
            ),
            likes = likes,
            reactions = reactions,
            interactionContext = interactionContext,
        )
    }

    private suspend fun loadAssets(
        feedItemId: Uuid,
    ): List<MediaAsset> {
        return when (
            val result =
                mediaAssetRepository.findByFeedItemId(
                    feedItemId = feedItemId,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
                    .asSequence()
                    .filter { media ->
                        media.status == MediaStatus.READY
                    }
                    .filter { media ->
                        media.deletedAt == null
                    }
                    .filter { media ->
                        media.purpose ==
                            MediaPurpose.FEED_ATTACHMENT
                    }
                    .map { media ->
                        media.toContract()
                    }
                    .toList()
            }

            is RepositoryResult.Error -> {
                emptyList()
            }
        }
    }

    private suspend fun loadLikes(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): PagedResult<User> {
        return when (
            val result =
                feedItemRepository.findLikeUserIds(
                    feedItemId = feedItemId,
                    limit = DEFAULT_LIKES_LIMIT,
                    offset = 0L,
                )
        ) {
            is RepositoryResult.Success -> {
                val page = result.data

                if (page.items.isEmpty()) {
                    return page.toPagedResult(
                        items = emptyList(),
                    )
                }

                val usersById = loadUsersById(
                    userIds = page.items,
                )

                /*
                 * We bouwen de lijst opnieuw op in dezelfde
                 * volgorde als de like-query.
                 */
                val userRecords = page.items
                    .mapNotNull { userId ->
                        usersById[userId]
                    }

                val users = userContextLoader
                    .loadAll(
                        currentUserId = currentUserId,
                        targetUsers = userRecords,
                    )
                    .map { userWithContext ->
                        userWithContext.user
                    }

                page.toPagedResult(
                    items = users,
                )
            }

            is RepositoryResult.Error -> {
                emptyPagedResult(
                    limit = DEFAULT_LIKES_LIMIT,
                )
            }
        }
    }

    private suspend fun loadReactions(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): PagedResult<FeedItemComment> {
        return when (
            val result =
                reactionRepository.findByFeedItemId(
                    feedItemId = feedItemId,
                    parentReactionId = null,
                    limit = DEFAULT_REACTIONS_LIMIT,
                    offset = 0L,
                )
        ) {
            is RepositoryResult.Success -> {
                reactionContextLoader.loadCommentsPage(
                    currentUserId = currentUserId,
                    page = result.data,
                )
            }

            is RepositoryResult.Error -> {
                emptyPagedResult(
                    limit = DEFAULT_REACTIONS_LIMIT,
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

    private fun decodeMessage(
        messageJson: String?,
    ): RichTextDocument? {
        if (messageJson.isNullOrBlank()) {
            return null
        }

        return Json.decodeFromString<RichTextDocument>(
            messageJson,
        )
    }

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
        const val DEFAULT_REACTIONS_LIMIT = 20
    }
}