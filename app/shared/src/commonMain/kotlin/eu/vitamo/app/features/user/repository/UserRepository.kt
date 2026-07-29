package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface UserRepository {
    suspend fun getCurrentUser() : RepositoryResult<UserWithContext>

    suspend fun getUser(
        userId: Uuid,
    ): RepositoryResult<UserWithContext>

    suspend fun searchUsers(
        query: String?,
        limit: Int = 20,
        offset: Long = 0,
    ): RepositoryResult<PagedResult<UserWithContext>>
}