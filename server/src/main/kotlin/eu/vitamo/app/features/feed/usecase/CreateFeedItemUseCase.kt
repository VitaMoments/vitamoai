package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.features.feed.mapper.toFeedItem
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CreateFeedItemUseCase(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val validator: FeedInputValidator,
) {
    suspend operator fun invoke(userId: Uuid, request: CreateFeedItemRequest): FeedItem {
        validator.validateCreate(request)
        val now = Clock.System.now()
        val record = feedRepository.create(userId, request, now)
        val author = userRepository.findById(record.authorId) ?: throw FeedException.NotFound("Author not found")
        return record.toFeedItem(author)
    }
}
