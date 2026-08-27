package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.request.CreatePostRequest
import eu.vitamo.app.features.feed.model.FeedMediaUpload
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class CreateFeedItemUseCase(
    private val createPostUseCase: CreatePostUseCase
) {
    suspend operator fun invoke(
        userId: Uuid,
        request: CreateFeedItemRequest?,
        files: MutableList<FeedMediaUpload>,
        ) : RepositoryResult<FeedItem> = when(request) {
            is CreatePostRequest -> createPostUseCase(userId, title= request.content.title, message=request.content.message, files = files)
            else -> createPostUseCase(userId, title= request?.content?.title, message=request?.content?.message, files = files)
        }
}