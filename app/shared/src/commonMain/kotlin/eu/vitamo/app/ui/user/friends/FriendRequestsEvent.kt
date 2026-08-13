package eu.vitamo.app.ui.user.friends

import kotlin.uuid.Uuid

sealed interface FriendRequestsEvent {

    data object LoadMore : FriendRequestsEvent

    data object Retry : FriendRequestsEvent

    data object RetryLoadMore : FriendRequestsEvent

    data class AcceptFriendRequest(
        val userId : Uuid,
        val friendshipId: Uuid
    ) : FriendRequestsEvent

    data class RejectFriendRequest(
        val userId: Uuid,
        val friendshipId: Uuid,
    ) : FriendRequestsEvent

    data class RevokeFriendRequest(
        val userId : Uuid,
        val friendshipId: Uuid
    ): FriendRequestsEvent
}