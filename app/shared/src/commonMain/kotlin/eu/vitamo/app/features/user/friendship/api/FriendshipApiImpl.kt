package eu.vitamo.app.features.user.friendship.api

import eu.vitamo.app.api.contracts.friendship.SendFriendRequestRequest
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.uuid.Uuid

class FriendshipApiImpl(
    private val client: HttpClient,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : FriendshipApi {

    override suspend fun sendRequest(
        targetUserId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.post(
                urlString =
                    "friendships/requests",
            ) {
                contentType(
                    ContentType.Application.Json,
                )

                setBody(
                    SendFriendRequestRequest(
                        targetUserId = targetUserId,
                    ),
                )
            }
        }
    }

    override suspend fun acceptRequest(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.post(
                urlString =
                    "friendships/requests/$friendshipId/accept",
            )
        }
    }

    override suspend fun deleteRequest(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.delete(
                urlString =
                    "friendships/requests/$friendshipId",
            )
        }
    }

    override suspend fun removeFriendship(
        friendshipId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.delete(
                urlString =
                    "friendships/$friendshipId",
            )
        }
    }

    override suspend fun getIncomingRequests(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString =
                    "friendships/requests/incoming",
            ) {
                parameter(
                    key = "limit",
                    value = limit,
                )

                parameter(
                    key = "offset",
                    value = offset,
                )
            }
        }
    }

    override suspend fun getOutgoingRequests(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString =
                    "friendships/requests/outgoing",
            ) {
                parameter(
                    key = "limit",
                    value = limit,
                )

                parameter(
                    key = "offset",
                    value = offset,
                )
            }
        }
    }

    override suspend fun getFriendRequests(
        limit: Int,
        offset: Long
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString =
                    "friendships/requests",
            ) {
                parameter(
                    key = "limit",
                    value = limit,
                )

                parameter(
                    key = "offset",
                    value = offset,
                )
            }
        }
    }

    override suspend fun getFriends(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(urlString = "friendships/") {
                parameter(
                    key = "limit",
                    value = limit,
                )

                parameter(
                    key = "offset",
                    value = offset,
                )
            }
        }
    }
}