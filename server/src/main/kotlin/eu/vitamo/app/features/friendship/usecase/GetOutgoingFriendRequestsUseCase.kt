package eu.vitamo.app.features.friendship.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.friendship.usecase.helper.FriendshipPageLoader
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class GetOutgoingFriendRequestsUseCase(
    private val friendshipRepository: FriendshipRepository,
    private val friendshipPageLoader: FriendshipPageLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        return when (
            val result =
                friendshipRepository
                    .findOutgoingRequests(
                        currentUserId =
                            currentUserId,
                        limit = limit,
                        offset = offset,
                    )
        ) {
            is RepositoryResult.Success -> {
                friendshipPageLoader.load(
                    currentUserId =
                        currentUserId,
                    page = result.data,
                )
            }

            is RepositoryResult.Error -> {
                result
            }
        }
    }
}