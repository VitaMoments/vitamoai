package eu.vitamo.app.features.friendship.mapper

import eu.vitamo.app.api.contracts.friendship.FriendshipContext
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.features.friendship.model.FriendshipStatus
import kotlin.uuid.Uuid

fun FriendshipRecord.toFriendshipContext(
    currentUserId: Uuid,
): FriendshipContext {
    val state = when {
        status == FriendshipStatus.ACCEPTED -> {
            FriendshipState.FRIENDS
        }

        requestedById == currentUserId -> {
            FriendshipState.OUTGOING_REQUEST
        }

        else -> {
            FriendshipState.INCOMING_REQUEST
        }
    }

    return FriendshipContext(
        friendshipId = id,
        state = state,
    )
}