package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.FeedItemsPageResponse
import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.features.feed.mapper.toFeedItem
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.service.FeedFriendshipService
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.uuid.Uuid

class GetFeedPageUseCase(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val friendshipService: FeedFriendshipService,
) {
    suspend fun myFeed(userId: Uuid, limit: Int, offset: Int): FeedItemsPageResponse {
        validatePagination(limit, offset)
        val (records, total) = feedRepository.getForUser(userId, limit, offset)
        val items = records.map { record ->
            val author = userRepository.findById(record.authorId) ?: throw FeedException.NotFound("Author not found")
            record.toFeedItem(author)
        }

        return FeedItemsPageResponse(
            items = items,
            total = total,
            limit = limit,
            offset = offset,
            hasMore = offset + items.size < total,
        )
    }

    suspend fun generalFeed(
        viewerId: Uuid,
        limit: Int,
        offset: Int,
        categories: Set<FeedCategory>,
    ): FeedItemsPageResponse {
        validatePagination(limit, offset)
        val (records, _) = feedRepository.getAllVisibleCandidates(
            limit = maxOf(limit * 3, 20),
            offset = 0,
            categories = categories,
        )

        val visibleRecords = records.filter { record ->
            when (record.privacy) {
                PrivacyStatus.PUBLIC -> true
                PrivacyStatus.PRIVATE -> record.authorId == viewerId
                PrivacyStatus.FRIENDS_ONLY -> record.authorId == viewerId || friendshipService.areFriends(viewerId, record.authorId)
            }
        }

        val paged = visibleRecords.drop(offset).take(limit)

        val items = paged.map { record ->
            val author = userRepository.findById(record.authorId) ?: throw FeedException.NotFound("Author not found")
            record.toFeedItem(author)
        }

        return FeedItemsPageResponse(
            items = items,
            total = visibleRecords.size.toLong(),
            limit = limit,
            offset = offset,
            hasMore = offset + items.size < visibleRecords.size,
        )
    }

    private fun validatePagination(limit: Int, offset: Int) {
        if (limit !in 1..100 || offset < 0) {
            throw FeedException.InvalidPagination()
        }
    }
}
