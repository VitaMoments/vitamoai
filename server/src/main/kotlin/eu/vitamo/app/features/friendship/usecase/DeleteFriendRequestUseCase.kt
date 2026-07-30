package eu.vitamo.app.features.friendship.usecase

import eu.vitamo.app.api.contracts.friendship.FriendshipMutationResponse
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class DeleteFriendRequestUseCase(
    private val friendshipRepository: FriendshipRepository,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        friendshipId: Uuid,
    ): RepositoryResult<FriendshipMutationResponse> {
        return when (
            val result =
                friendshipRepository
                    .deletePendingRequest(
                        friendshipId =
                            friendshipId,
                        currentUserId =
                            currentUserId,
                    )
        ) {
            is RepositoryResult.Success -> {
                RepositoryResult.Success(
                    data =
                        FriendshipMutationResponse(
                            friendshipId = null,
                            state =
                                FriendshipState.NONE,
                        ),
                )
            }

            is RepositoryResult.Error -> {
                result
            }
        }
    }
}