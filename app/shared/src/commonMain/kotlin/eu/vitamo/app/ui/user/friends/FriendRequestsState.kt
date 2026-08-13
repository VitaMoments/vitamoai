package eu.vitamo.app.ui.user.friends

import eu.vitamo.app.api.contracts.user.UserWithContext
import kotlin.uuid.Uuid

data class FriendRequestsState(
    val query: String = "",
    val users: List<UserWithContext> = emptyList(),

    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,

    val nextOffset: Long? = 0L,

    val initialError: String? = null,
    val loadMoreError: String? = null,

    val friendshipActionsInProgress: Set<Uuid> = emptySet(),
) {
    val isEmpty: Boolean
        get() = !isInitialLoading &&
                initialError == null &&
                users.isEmpty()

    fun isFriendshipActionInProgress(
        userId: Uuid,
    ): Boolean {
        return userId in friendshipActionsInProgress
    }
}