package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.repository.FeedRepository
import kotlin.time.Clock
import kotlin.uuid.Uuid

class DeleteFeedItemUseCase(
    private val feedRepository: FeedRepository,
) {
    suspend operator fun invoke(userId: Uuid, feedItemId: Uuid) {
        val deleted = feedRepository.softDelete(
            uuid = feedItemId,
            authorId = userId,
            now = Clock.System.now(),
        )
        if (!deleted) {
            throw FeedException.Forbidden("You do not have permission to delete this feed item")
        }
    }
}
