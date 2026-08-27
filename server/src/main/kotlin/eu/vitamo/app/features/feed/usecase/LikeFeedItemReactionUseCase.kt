package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid

class LikeFeedItemReactionUseCase(
    private val reactionRepository: FeedItemReactionRepository,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        reactionId: Uuid,
    ): RepositoryResult<Unit> =
        reactionRepository.like(
            reactionId = reactionId,
            userId = currentUserId,
            createdAt = Clock.System.now(),
        )
}