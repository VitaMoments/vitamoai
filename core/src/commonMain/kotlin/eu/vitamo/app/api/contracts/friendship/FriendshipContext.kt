package eu.vitamo.app.api.contracts.friendship

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class FriendshipContext(
    val friendshipId: Uuid? = null,
    val state: FriendshipState = FriendshipState.NONE
)