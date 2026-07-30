package eu.vitamo.app.features.friendship.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class AcceptFriendRequestUseCase(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext> {
        val friendship = when (
            val result =
                friendshipRepository.acceptRequest(
                    friendshipId =
                        friendshipId,
                    currentUserId =
                        currentUserId,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                return result
            }
        }

        val otherUser = when (
            val result =
                userRepository.findById(
                    id = friendship.otherUserId(
                        currentUserId =
                            currentUserId,
                    ),
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                return result
            }
        }

        return RepositoryResult.Success(
            data = userContextLoader.load(
                currentUserId =
                    currentUserId,
                targetUser =
                    otherUser,
            ),
        )
    }
}