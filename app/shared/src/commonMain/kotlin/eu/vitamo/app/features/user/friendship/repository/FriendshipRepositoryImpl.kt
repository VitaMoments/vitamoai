package eu.vitamo.app.features.user.friendship.repository

import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.user.friendship.api.FriendshipApi
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FriendshipRepositoryImpl(
    private val friendshipApi: FriendshipApi,
) : FriendshipRepository {

    override suspend fun sendRequest(
        targetUserId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return friendshipApi
            .sendRequest(
                targetUserId = targetUserId,
            )
            .toRepositoryResult()
    }

    override suspend fun acceptRequest(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return friendshipApi
            .acceptRequest(
                friendshipId = friendshipId,
            )
            .toRepositoryResult()
    }

    override suspend fun deleteRequest(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return friendshipApi
            .deleteRequest(
                friendshipId = friendshipId,
            )
            .toRepositoryResult()
    }

    override suspend fun removeFriendship(
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return friendshipApi
            .removeFriendship(
                friendshipId = friendshipId,
            )
            .toRepositoryResult()
    }

    override suspend fun getIncomingRequests(
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return friendshipApi
            .getIncomingRequests(
                limit = limit,
                offset = offset,
            )
            .toRepositoryResult()
    }

    override suspend fun getOutgoingRequests(
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return friendshipApi
            .getOutgoingRequests(
                limit = limit,
                offset = offset,
            )
            .toRepositoryResult()
    }

    override suspend fun getFriendRequests(
        limit: Int,
        offset: Long,
        state: FriendshipState?
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return friendshipApi
            .getFriendRequests(
                limit = limit,
                offset = offset
            )
            .toRepositoryResult()
    }

    override suspend fun getFriends(
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return friendshipApi
            .getFriends(
                limit = limit,
                offset = offset,
            )
            .toRepositoryResult()
    }
}