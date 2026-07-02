package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.mapper.toFeedItem
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.time.Clock
import kotlin.uuid.Uuid

class UpdateFeedItemUseCase(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val validator: FeedInputValidator,
) {
    suspend operator fun invoke(userId: Uuid, feedItemId: Uuid, request: UpdateFeedItemRequest): FeedItem {
        validator.validateUpdate(request)
        val record = feedRepository.update(
            uuid = feedItemId,
            authorId = userId,
            request = request,
            now = Clock.System.now(),
        ) ?: throw FeedException.Forbidden("You do not have permission to update this feed item")

        val author = userRepository.findById(record.authorId) ?: throw FeedException.NotFound("Author not found")
        return record.toFeedItem(author)
    }
}
