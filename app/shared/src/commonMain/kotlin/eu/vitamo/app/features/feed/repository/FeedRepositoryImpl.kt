package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.feed.api.FeedApi
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.repository.RepositoryResult

class FeedRepositoryImpl(
    private val feedApi: FeedApi,
) : FeedRepository {

    override suspend fun getFeed(
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<FeedItem>> {
        return feedApi.getFeed(
            limit = limit,
            offset = offset,
        ).toRepositoryResult { response ->
            response
        }
    }

    override suspend fun createFeedItem(
        request: CreateFeedItemRequest,
        images: List<PickedImage>,
    ): RepositoryResult<FeedItem> {
        return feedApi.createFeedItem(
            request = request,
            images = images,
        ).toRepositoryResult { response ->
            response
        }
    }
}