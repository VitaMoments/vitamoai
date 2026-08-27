package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UnlikeFeedItemReactionUseCase(
    private val reactionRepository: FeedItemReactionRepository,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        reactionId: Uuid,
    ): RepositoryResult<Unit> =
        reactionRepository.unlike(
            reactionId = reactionId,
            userId = currentUserId,
        )
}