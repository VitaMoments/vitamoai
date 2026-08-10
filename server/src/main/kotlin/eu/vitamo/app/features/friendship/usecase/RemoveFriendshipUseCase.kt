package eu.vitamo.app.features.friendship.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class RemoveFriendshipUseCase(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository,
    private val userContextLoader: UserContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        friendshipId: Uuid,
    ): RepositoryResult<UserWithContext> {
        return when (
            val deleteResult = friendshipRepository.deleteFriendship(
                friendshipId = friendshipId,
                currentUserId = currentUserId,
            )
        ) {
            is RepositoryResult.Success -> {
                val targetUserId = deleteResult.data.otherUserId(
                    currentUserId = currentUserId,
                )

                when (
                    val userResult = userRepository.findById(
                        id = targetUserId,
                    )
                ) {
                    is RepositoryResult.Success -> {
                        RepositoryResult.Success(
                            data = userContextLoader.load(
                                currentUserId = currentUserId,
                                targetUser = userResult.data,
                            ),
                        )
                    }

                    is RepositoryResult.Error -> {
                        userResult
                    }
                }
            }

            is RepositoryResult.Error -> {
                deleteResult
            }
        }
    }
}