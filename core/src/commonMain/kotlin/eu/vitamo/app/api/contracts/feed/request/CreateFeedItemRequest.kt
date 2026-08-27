package eu.vitamo.app.api.contracts.feed.request

import eu.vitamo.app.api.contracts.feed.FeedItemContent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateFeedItemRequest {
    val content: FeedItemContent
}

@Serializable
@SerialName("POST")
data class CreatePostRequest(
    override val content: FeedItemContent
) : CreateFeedItemRequest