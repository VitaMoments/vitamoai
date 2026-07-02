package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.user.PublicUser
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class FeedItem(
    @Contextual
    override val uuid: Uuid,
    override val author: PublicUser,
    override val content: RichTextDocument,
    override val privacy: PrivacyStatus,
    val categories: List<FeedCategory> = emptyList(),
    val mediaAssets: List<MediaAsset> = emptyList(),
    @Contextual
    override val createdAt: Instant,
    @Contextual
    override val updatedAt: Instant,
    @Contextual
    override val deletedAt: Instant? = null,
) : BaseFeedItem
