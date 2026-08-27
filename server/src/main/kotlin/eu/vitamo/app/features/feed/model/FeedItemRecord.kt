package eu.vitamo.app.features.feed.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed interface FeedItemRecord {
    val id: Uuid
    val authorId: Uuid
    val createdAt: Instant
    val updatedAt: Instant
    val deletedAt: Instant?
}