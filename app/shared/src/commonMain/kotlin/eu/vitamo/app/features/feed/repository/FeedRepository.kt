package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.repository.RepositoryResult

interface FeedRepository {

    suspend fun getFeed(
        limit: Int = 20,
        offset: Long = 0L,
    ): RepositoryResult<PagedResult<FeedItem>>

    suspend fun createFeedItem(
        request: CreateFeedItemRequest,
        images: List<PickedImage>,
    ): RepositoryResult<FeedItem>
}