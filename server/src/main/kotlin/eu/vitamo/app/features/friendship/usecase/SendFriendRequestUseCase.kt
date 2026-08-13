package eu.vitamo.app.features.friendship.usecase

import com.google.firebase.database.core.Repo
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.device.model.DeviceRecord
import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.features.friendship.error.FriendshipRepositoryErrors
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.notification.push.model.PushNotification
import eu.vitamo.app.infrastructure.notification.push.service.PushNotificationService
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class SendFriendRequestUseCase(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val friendshipRepository: FriendshipRepository,
    private val userContextLoader: UserContextLoader,
    private val notificationService: PushNotificationService
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
        val currentUser = when (val result = userRepository.findById(currentUserId)) {
            is RepositoryResult.Success -> { result.data }
            is RepositoryResult.Error -> return result
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
            is RepositoryResult.Success -> {
                when(val devicesResult = deviceRepository.findActiveByUserId(targetUserId)) {
                    is RepositoryResult.Success<List<DeviceRecord>> -> {
                        runCatching {
                            val ids = devicesResult.data.mapNotNull { it.firebaseInstallationId }
                            notificationService.sendToDevices(ids, PushNotification.FriendRequestReceived(currentUser.id, currentUser.displayName))
                        }
                    }
                    else -> Unit
                }
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
                    targetUser,
            ),
        )
    }
}