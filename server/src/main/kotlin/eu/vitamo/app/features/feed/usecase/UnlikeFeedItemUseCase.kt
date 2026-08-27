package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UnlikeFeedItemUseCase(
    private val feedItemRepository: FeedItemRepository,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): RepositoryResult<Unit> =
        feedItemRepository.unlike(
            feedItemId = feedItemId,
            userId = currentUserId,
        )
}