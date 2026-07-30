package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.features.user.api.UserApi
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UserRepositoryImpl(
    private val userApi: UserApi,
) : UserRepository {

    override suspend fun getUser(
        userId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return userApi
            .getUser(userId)
            .toRepositoryResult()
    }

    override suspend fun getCurrentUser(): RepositoryResult<UserWithContext> {
        return userApi
            .getCurrentUser()
            .toRepositoryResult()
    }

    override suspend fun searchUsers(
        query: String?,
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return userApi
            .searchUsers(
                query = query,
                limit = limit,
                offset = offset,
            )
            .toRepositoryResult()
    }

    override suspend fun updateProfileImage(
        image: PickedImage,
    ): RepositoryResult<UserWithContext> {
        return userApi
            .updateProfileImage(
                image = image,
            )
            .toRepositoryResult { response ->
                response
            }
    }

    private companion object {
        const val MAX_PAGE_SIZE: Int = 50
    }
}