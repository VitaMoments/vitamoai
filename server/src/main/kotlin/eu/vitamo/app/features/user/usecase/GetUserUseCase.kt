package eu.vitamo.app.features.user.usecase

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.extension_functions.mapSuspend
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class GetUserUseCase(
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
) {
    suspend operator fun invoke(
        currentUserId: Uuid,
        userId: Uuid,
    ): RepositoryResult<UserWithContext> =
        userRepository.findById(userId)
            .mapSuspend { userRecord ->
                userContextLoader.load(
                    currentUserId = currentUserId,
                    targetUser = userRecord,
                )
            }
}