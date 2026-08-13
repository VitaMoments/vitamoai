package eu.vitamo.app.features.user.friendship.api

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import kotlin.uuid.Uuid

interface FriendshipApi {

    suspend fun sendRequest(
        targetUserId: Uuid,
    ): ApiResult<UserWithContext>

    suspend fun acceptRequest(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext>

    /**
     * Verwijdert een openstaand verzoek.
     *
     * Voor de verzender betekent dit intrekken.
     * Voor de ontvanger betekent dit weigeren.
     */
    suspend fun deleteRequest(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext>

    suspend fun removeFriendship(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext>

    suspend fun getIncomingRequests(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>>

    suspend fun getOutgoingRequests(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>>

    suspend fun getFriendRequests(
        limit: Int,
        offset: Long
    ): ApiResult<PagedResult<UserWithContext>>

    suspend fun getFriends(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>>
}