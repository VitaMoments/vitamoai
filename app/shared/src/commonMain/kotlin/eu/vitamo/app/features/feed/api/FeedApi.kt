package eu.vitamo.app.features.feed.api

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemsPageResponse
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.api.result.ApiResult
import kotlin.uuid.Uuid

interface FeedApi {
    suspend fun createFeedItem(request: CreateFeedItemRequest): ApiResult<FeedItem>
    suspend fun getFeedItem(uuid: Uuid): ApiResult<FeedItem>
    suspend fun getMyFeed(limit: Int, offset: Int): ApiResult<FeedItemsPageResponse>
    suspend fun getGeneralFeed(limit: Int, offset: Int, categories: Set<String> = emptySet()): ApiResult<FeedItemsPageResponse>
    suspend fun updateFeedItem(uuid: Uuid, request: UpdateFeedItemRequest): ApiResult<FeedItem>
    suspend fun deleteFeedItem(uuid: Uuid): ApiResult<Unit>
}
