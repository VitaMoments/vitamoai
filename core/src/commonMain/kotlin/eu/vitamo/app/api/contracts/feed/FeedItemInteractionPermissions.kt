package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedItemInteractionPermissions(
    val canReply: FeedItemPermission = FeedItemPermission.DENIED,
    val canLike: FeedItemPermission = FeedItemPermission.DENIED,
    val canEdit: FeedItemPermission = FeedItemPermission.DENIED,
    val canDelete: FeedItemPermission = FeedItemPermission.DENIED,
)
