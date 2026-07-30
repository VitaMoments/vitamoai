package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.friendship.FriendshipContext
import eu.vitamo.app.api.contracts.media.MediaReference
import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.friendship.mapper.toFriendshipContext
import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.user.mapper.toUser
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UserContextLoader(
    private val contextProvider: UserContextProvider,
    private val mediaAssetRepository: MediaAssetRepository,
    private val friendshipRepository: FriendshipRepository,
) {

    suspend fun load(
        currentUserId: Uuid,
        targetUser: UserRecord,
    ): UserWithContext {
        val context = contextProvider.resolve(
            currentUserId = currentUserId,
            targetUser = targetUser,
        )

        val profileImage = loadProfileImage(
            user = targetUser,
        )

        val friendshipContext = loadFriendshipContext(
            currentUserId = currentUserId,
            targetUserId = targetUser.id,
        )

        return UserWithContext(
            user = targetUser.toUser(
                accessLevel = context.accessLevel,
                profileImage = profileImage,
            ),
            friendshipContext = friendshipContext,
        )
    }

//    suspend fun loadAll(
//        currentUserId: Uuid,
//        targetUsers: List<UserRecord>,
//    ): List<UserWithContext> =
//        targetUsers.map { targetUser ->
//            load(
//                currentUserId = currentUserId,
//                targetUser = targetUser,
//            )
//        }

    suspend fun loadAll(
        currentUserId: Uuid,
        targetUsers: List<UserRecord>,
    ): List<UserWithContext> {
        if (targetUsers.isEmpty()) {
            return emptyList()
        }

        val friendshipsByUserId =
            loadFriendshipsByUserId(
                currentUserId = currentUserId,
                targetUsers = targetUsers,
            )

        return targetUsers.map { targetUser ->
            val accessContext =
                contextProvider.resolve(
                    currentUserId =
                        currentUserId,
                    targetUser =
                        targetUser,
                )
            val profileImage =
                loadProfileImage(
                    user = targetUser,
                )

            val friendshipContext =
                if (
                    currentUserId ==
                    targetUser.id
                ) {
                    FriendshipContext()
                } else {
                    friendshipsByUserId[
                        targetUser.id
                    ]?.toFriendshipContext(
                        currentUserId =
                            currentUserId,
                    ) ?: FriendshipContext()
                }

            UserWithContext(
                user = targetUser.toUser(
                    accessLevel =
                        accessContext.accessLevel,
                    profileImage =
                        profileImage,
                ),
                friendshipContext = friendshipContext,
            )
        }
    }

    private suspend fun loadFriendshipsByUserId(
        currentUserId: Uuid?,
        targetUsers: List<UserRecord>,
    ): Map<Uuid, FriendshipRecord> {
        if (currentUserId == null) {
            return emptyMap()
        }

        return when (
            val result =
                friendshipRepository
                    .findBetweenUserAndTargets(
                        currentUserId =
                            currentUserId,
                        targetUserIds =
                            targetUsers.map(
                                UserRecord::id,
                            ),
                    )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                emptyMap()
            }
        }
    }

    private suspend fun loadFriendshipContext(
        currentUserId: Uuid?,
        targetUserId: Uuid,
    ): FriendshipContext {
        if (
            currentUserId == null ||
            currentUserId == targetUserId
        ) {
            return FriendshipContext()
        }

        return when (
            val result =
                friendshipRepository
                    .findBetweenUsers(
                        firstUserId =
                            currentUserId,
                        secondUserId =
                            targetUserId,
                    )
        ) {
            is RepositoryResult.Success -> {
                result.data
                    ?.toFriendshipContext(
                        currentUserId =
                            currentUserId,
                    )
                    ?: FriendshipContext()
            }

            is RepositoryResult.Error -> {
                FriendshipContext()
            }
        }
    }

    private suspend fun loadProfileImage(
        user: UserRecord,
    ): MediaReference? {
        val profileImageId = user.profileImageId
            ?: return null

        return when (
            val result = mediaAssetRepository.findById(
                id = profileImageId,
            )
        ) {
            is RepositoryResult.Success -> {
                val media = result.data

                if (
                    media.status != MediaStatus.READY ||
                    media.deletedAt != null
                ) {
                    null
                } else {
                    MediaReference(
                        id = media.id,
                        contentPath = "/media/${media.id}/content",
                    )
                }
            }

            is RepositoryResult.Error ->
                null
        }
    }
}