package eu.vitamo.app.api.contracts.friendship

import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipState {
    NONE,
    OUTGOING_REQUEST,
    INCOMING_REQUEST,
    FRIENDS,
}