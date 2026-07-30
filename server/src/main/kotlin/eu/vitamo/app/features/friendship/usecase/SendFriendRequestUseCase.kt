package eu.vitamo.app.features.friendship.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.friendship.error.FriendshipRepositoryErrors
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class SendFriendRequestUseCase(
    private val userRepository: UserRepository,
    private val friendshipRepository:
    FriendshipRepository,
    private val userContextLoader:
    UserContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        targetUserId: Uuid,
    ): RepositoryResult<UserWithContext> {
        if (currentUserId == targetUserId) {
            return RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .cannotFriendSelf(),
            )
        }

        val targetUser = when (
            val result = userRepository.findById(
                id = targetUserId,
            )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                return result
            }
        }

        when (
            val result =
                friendshipRepository.createRequest(
                    requesterId =
                        currentUserId,
                    targetUserId =
                        targetUserId,
                )
        ) {
            is RepositoryResult.Success -> Unit

            is RepositoryResult.Error -> {
                return result
            }
        }

        return RepositoryResult.Success(
            data = userContextLoader.load(
                currentUserId =
                    currentUserId,
                targetUser =
                    targetUser,
            ),
        )
    }
}