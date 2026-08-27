package eu.vitamo.app.features.feed.api

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.model.PickedImage

interface FeedApi {
    suspend fun getFeed(
        limit: Int = 20,
        offset: Long = 0L,
    ): ApiResult<PagedResult<FeedItem>>

    suspend fun createFeedItem(
        request: CreateFeedItemRequest,
        images: List<PickedImage>,
    ): ApiResult<FeedItem>
}