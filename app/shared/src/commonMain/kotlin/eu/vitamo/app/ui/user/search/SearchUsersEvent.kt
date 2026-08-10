package eu.vitamo.app.ui.user.search

import kotlin.uuid.Uuid

sealed interface SearchUsersEvent {

    data class QueryChanged(
        val query: String,
    ) : SearchUsersEvent

    data object SearchClicked : SearchUsersEvent

    data object LoadMore : SearchUsersEvent

    data object Retry : SearchUsersEvent

    data object RetryLoadMore : SearchUsersEvent

    data class SendFriendRequest(
        val userId : Uuid
    ) : SearchUsersEvent

    data class AcceptFriendRequest(
        val userId : Uuid,
        val friendshipId: Uuid
    ) : SearchUsersEvent

    data class RejectFriendRequest(
        val userId: Uuid,
        val friendshipId: Uuid,
    ) : SearchUsersEvent

    data class RevokeFriendRequest(
        val userId : Uuid,
        val friendshipId: Uuid
    ): SearchUsersEvent

    data class RemoveFriendship(
        val userId: Uuid,
        val friendshipId: Uuid,
    ) : SearchUsersEvent
}