package eu.vitamo.app.features.user.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.extension_functions.mapSuspend
import eu.vitamo.app.infrastructure.network.models.toPagedResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class SearchUsersUseCase(
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
) {
    suspend operator fun invoke(
        currentUserId: Uuid,
        query: String?,
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> =
        userRepository.search(
            currentUserId = currentUserId,
            query = query,
            limit = limit,
            offset = offset,
        ).mapSuspend { page ->
            val usersWithContext = userContextLoader.loadAll(
                currentUserId = currentUserId,
                targetUsers = page.items,
            )

            page.toPagedResult(
                mappedItems = usersWithContext,
            )
        }
}