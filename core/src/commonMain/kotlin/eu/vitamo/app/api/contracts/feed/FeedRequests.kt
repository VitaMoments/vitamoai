package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable

@Serializable
data class CreateFeedItemRequest(
    val content: RichTextDocument,
    val privacy: PrivacyStatus,
    val categories: List<FeedCategory> = emptyList(),
    val mediaAssets: List<MediaAsset> = emptyList(),
)

@Serializable
data class UpdateFeedItemRequest(
    val content: RichTextDocument? = null,
    val privacy: PrivacyStatus? = null,
    val categories: List<FeedCategory>? = null,
    val mediaAssets: List<MediaAsset>? = null,
)

@Serializable
data class FeedItemsPageResponse(
    val items: List<FeedItem>,
    val total: Long,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean,
)
