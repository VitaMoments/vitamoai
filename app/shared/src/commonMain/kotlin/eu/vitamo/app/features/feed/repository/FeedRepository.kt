package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemsPageResponse
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface FeedRepository {
    suspend fun createFeedItem(request: CreateFeedItemRequest): RepositoryResult<FeedItem>
    suspend fun getFeedItem(uuid: Uuid): RepositoryResult<FeedItem>
    suspend fun getMyFeed(limit: Int = 20, offset: Int = 0): RepositoryResult<FeedItemsPageResponse>
    suspend fun getGeneralFeed(limit: Int = 20, offset: Int = 0, categories: Set<String> = emptySet()): RepositoryResult<FeedItemsPageResponse>
    suspend fun updateFeedItem(uuid: Uuid, request: UpdateFeedItemRequest): RepositoryResult<FeedItem>
    suspend fun deleteFeedItem(uuid: Uuid): RepositoryResult<Unit>
}
