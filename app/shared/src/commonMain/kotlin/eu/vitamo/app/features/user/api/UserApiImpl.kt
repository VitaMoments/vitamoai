package eu.vitamo.app.features.user.api

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.uuid.Uuid

class UserApiImpl(
    private val client: HttpClient,
    private val config: UserApiConfig,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : UserApi {

    override suspend fun getUser(
        userId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString = "${config.baseUrl}/$userId",
            )
        }
    }

    override suspend fun getCurrentUser(): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator
        ) {
            client.get(
                urlString = "${config.baseUrl}/me"
            )
        }
    }

    override suspend fun searchUsers(
        query: String?,
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString = "${config.baseUrl}/search",
            ) {
                query
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let { value ->
                        parameter(
                            key = "query",
                            value = value,
                        )
                    }

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