package eu.vitamo.app.features.user.api

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import kotlin.uuid.Uuid

interface UserApi {

    suspend fun getUser(
        userId: Uuid,
    ): ApiResult<UserWithContext>

    suspend fun getCurrentUser() : ApiResult<UserWithContext>

    suspend fun searchUsers(
        query: String?,
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>>
}