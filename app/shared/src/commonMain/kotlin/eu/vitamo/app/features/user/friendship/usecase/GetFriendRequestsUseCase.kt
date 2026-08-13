package eu.vitamo.app.features.user.friendship.usecase

import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class GetFriendRequestsUseCase(
    private val friendshipRepository: FriendshipRepository,
) {
    suspend operator fun invoke(
        limit: Int,
        offset: Long,
        state: FriendshipState? = null,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return friendshipRepository.getFriendRequests(
            limit = limit,
            offset = offset,
            state = state,
        )
    }
}