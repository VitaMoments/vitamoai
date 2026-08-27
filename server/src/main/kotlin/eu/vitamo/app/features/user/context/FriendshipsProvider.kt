package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.friendship.FriendshipContext
import eu.vitamo.app.api.contracts.user.FriendshipUser
import eu.vitamo.app.api.result.PagedResult
import kotlin.uuid.Uuid

interface FriendshipsProvider {

    suspend fun resolve(
        currentUserId: Uuid,
        targetUserId: Uuid,
        friendshipContext: FriendshipContext,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = 0L,
    ): PagedResult<FriendshipUser>?

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}