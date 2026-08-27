package eu.vitamo.app.features.feed.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class PostRecord(
    override val id: Uuid,
    override val authorId: Uuid,
    val title: String?,
    val messageJson: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : FeedItemRecord