package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid

class LikeFeedItemUseCase(
    private val feedItemRepository: FeedItemRepository,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        feedItemId: Uuid,
    ): RepositoryResult<Unit> =
        feedItemRepository.like(
            feedItemId = feedItemId,
            userId = currentUserId,
            createdAt = Clock.System.now(),
        )
}