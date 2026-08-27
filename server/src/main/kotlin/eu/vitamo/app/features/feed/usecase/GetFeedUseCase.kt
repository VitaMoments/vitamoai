package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.feed.context.FeedItemContextLoader
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.repository.RepositoryResult
import eu.vitamo.app.repository.flatMap
import eu.vitamo.app.repository.flatMapSuspend
import eu.vitamo.app.repository.mapSuspend
import kotlin.uuid.Uuid

class GetFeedUseCase(
    private val friendshipRepository: FriendshipRepository,
    private val feedItemRepository: FeedItemRepository,
    private val feedItemContextLoader: FeedItemContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        limit: Int = DEFAULT_LIMIT,
        offset: Long = 0L,
    ): RepositoryResult<PagedResult<FeedItem>> {
        return friendshipRepository
            .findFriendIds(
                currentUserId = currentUserId,
            )
            .flatMapSuspend { friendIds ->
                feedItemRepository
                    .findFeedForUser(
                        currentUserId = currentUserId,
                        friendIds = friendIds,
                        limit = limit,
                        offset = offset,
                    )
                    .mapSuspend { page ->
                        feedItemContextLoader.loadPage(
                            currentUserId = currentUserId,
                            page = page,
                        )
                    }
            }
    }

    private companion object {
        const val DEFAULT_LIMIT = 20
    }
}