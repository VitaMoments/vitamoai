package eu.vitamo.app.features.user.friendship.repository

import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface FriendshipRepository {

    suspend fun sendRequest(
        targetUserId: Uuid,
    ): RepositoryResult<UserWithContext>

    suspend fun acceptRequest(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext>

    /**
     * Trekt een verstuurd verzoek in of weigert een ontvangen verzoek.
     */
    suspend fun deleteRequest(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext>

    suspend fun removeFriendship(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext>

    suspend fun getIncomingRequests(
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = DEFAULT_OFFSET,
    ): RepositoryResult<PagedResult<UserWithContext>>

    suspend fun getOutgoingRequests(
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = DEFAULT_OFFSET,
    ): RepositoryResult<PagedResult<UserWithContext>>

    suspend fun getFriendRequests(
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = DEFAULT_OFFSET,
        state: FriendshipState? = null
    ) : RepositoryResult<PagedResult<UserWithContext>>

    suspend fun getFriends(
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = DEFAULT_OFFSET,
    ): RepositoryResult<PagedResult<UserWithContext>>

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_OFFSET = 0L
    }
}