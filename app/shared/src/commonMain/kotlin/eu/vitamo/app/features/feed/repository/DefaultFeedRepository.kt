package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemsPageResponse
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.api.FeedApi
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class DefaultFeedRepository(
    private val feedApi: FeedApi,
) : FeedRepository {
    override suspend fun createFeedItem(request: CreateFeedItemRequest): RepositoryResult<FeedItem> {
        return feedApi.createFeedItem(request).toRepositoryResult { it }
    }

    override suspend fun getFeedItem(uuid: Uuid): RepositoryResult<FeedItem> {
        return feedApi.getFeedItem(uuid).toRepositoryResult { it }
    }

    override suspend fun getMyFeed(limit: Int, offset: Int): RepositoryResult<FeedItemsPageResponse> {
        return feedApi.getMyFeed(limit, offset).toRepositoryResult { it }
    }

    override suspend fun getGeneralFeed(limit: Int, offset: Int, categories: Set<String>): RepositoryResult<FeedItemsPageResponse> {
        return feedApi.getGeneralFeed(limit, offset, categories).toRepositoryResult { it }
    }

    override suspend fun updateFeedItem(uuid: Uuid, request: UpdateFeedItemRequest): RepositoryResult<FeedItem> {
        return feedApi.updateFeedItem(uuid, request).toRepositoryResult { it }
    }

    override suspend fun deleteFeedItem(uuid: Uuid): RepositoryResult<Unit> {
        return feedApi.deleteFeedItem(uuid).toRepositoryResult { Unit }
    }
}
