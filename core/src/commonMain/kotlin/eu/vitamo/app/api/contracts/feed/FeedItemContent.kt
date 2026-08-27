package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.media.MediaAsset
import kotlinx.serialization.Serializable

@Serializable
data class FeedItemContent(
    val title: String? = null,
    val message: RichTextDocument? = null,
    val assets: List<MediaAsset> = emptyList()
)
