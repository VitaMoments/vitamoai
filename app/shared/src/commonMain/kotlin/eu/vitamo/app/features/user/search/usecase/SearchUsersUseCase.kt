package eu.vitamo.app.features.user.search.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryResult

class SearchUsersUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        query: String?,
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return userRepository.searchUsers(
            query = query,
            limit = limit,
            offset = offset,
        )
    }
}