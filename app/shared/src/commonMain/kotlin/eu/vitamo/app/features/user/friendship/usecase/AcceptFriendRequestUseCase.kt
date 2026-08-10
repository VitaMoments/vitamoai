package eu.vitamo.app.features.user.friendship.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class AcceptFriendRequestUseCase(
    private val friendshipRepository: FriendshipRepository
) {
    suspend operator fun invoke(
        friendshipId: Uuid
    ) : RepositoryResult<UserWithContext> {
        return friendshipRepository.acceptRequest(friendshipId)
    }
}