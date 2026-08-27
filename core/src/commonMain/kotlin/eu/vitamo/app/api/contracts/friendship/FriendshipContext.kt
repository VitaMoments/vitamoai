package eu.vitamo.app.api.contracts.friendship

import eu.vitamo.app.api.contracts.user.FriendshipUser
import eu.vitamo.app.api.result.PagedResult
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class FriendshipContext(
    val friendshipId: Uuid? = null,
    val state: FriendshipState = FriendshipState.NONE,
    val friends: PagedResult<FriendshipUser>? = null
)