package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class FeedItemMetaData(
    val feedId: Uuid,
    val author: User,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant
)