package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedItemInteractionContext(
    val likedByMe: Boolean,
    val permissions: FeedItemInteractionPermissions
)
