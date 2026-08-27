package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.features.feed.context.FeedItemContextLoader
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class GetFeedItemUseCase(
    private val feedItemRepository: FeedItemRepository,
    private val feedItemContextLoader: FeedItemContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): RepositoryResult<FeedItem> {
        return when (
            val result = feedItemRepository.findById(
                feedItemId = feedItemId,
            )
        ) {
            is RepositoryResult.Success -> {
                val feedItem = feedItemContextLoader.load(
                    currentUserId = currentUserId,
                    feedItem = result.data,
                )
                    ?: return RepositoryResult.Error(
                        error = RepositoryError.Internal(
                            message = "Feed item could not be loaded",
                        ),
                    )

                RepositoryResult.Success(
                    data = feedItem,
                )
            }

            is RepositoryResult.Error -> {
                RepositoryResult.Error(
                    error = result.error,
                )
            }
        }
    }
}