package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.features.feed.mapper.toFeedItem
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.service.FeedFriendshipService
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.uuid.Uuid

class GetFeedItemUseCase(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val friendshipService: FeedFriendshipService,
) {
    suspend operator fun invoke(viewerId: Uuid, feedItemId: Uuid): FeedItem {
        val record = feedRepository.getById(feedItemId) ?: throw FeedException.NotFound()
        val allowed = when (record.privacy) {
            PrivacyStatus.PUBLIC -> true
            PrivacyStatus.PRIVATE -> record.authorId == viewerId
            PrivacyStatus.FRIENDS_ONLY -> record.authorId == viewerId || friendshipService.areFriends(viewerId, record.authorId)
        }
        if (!allowed) {
            throw FeedException.Forbidden("You do not have permission to view this feed item")
        }
        val author = userRepository.findById(record.authorId) ?: throw FeedException.NotFound("Author not found")
        return record.toFeedItem(author)
    }
}
