package eu.vitamo.app.features.feed.model

import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.MediaAsset
import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.api.contracts.feed.RichTextDocument
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class FeedItemRecord(
    val uuid: Uuid,
    val authorId: Uuid,
    val content: RichTextDocument,
    val privacy: PrivacyStatus,
    val categories: List<FeedCategory>,
    val mediaAssets: List<MediaAsset>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)
