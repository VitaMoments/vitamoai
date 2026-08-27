package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.repository.RepositoryResult

class CreateFeedItemUseCase(
    private val feedRepository: FeedRepository,
) {

    suspend operator fun invoke(
        request: CreateFeedItemRequest,
        images: List<PickedImage>,
    ): RepositoryResult<FeedItem> =
        feedRepository.createFeedItem(
            request = request,
            images = images,
        )
}