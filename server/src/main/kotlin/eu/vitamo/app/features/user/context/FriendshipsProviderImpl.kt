package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.friendship.FriendshipContext
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.FriendshipUser
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.friendship.mapper.toFriendshipContext
import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.user.mapper.toUser
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.network.models.toPagedResult
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FriendshipsProviderImpl(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
    private val mediaAssetRepository: MediaAssetRepository,
) : FriendshipsProvider {

    override suspend fun resolve(
        currentUserId: Uuid,
        targetUserId: Uuid,
        friendshipContext: FriendshipContext,
        limit: Int,
        offset: Long,
    ): PagedResult<FriendshipUser>? {
        val stateFilter =
            when {currentUserId == targetUserId -> { null }
                friendshipContext.state == FriendshipState.FRIENDS -> { FriendshipState.FRIENDS }
                else -> {
                    return null
                }
            }

        val page =
            when (val result = friendshipRepository
                .findFriendshipByState(
                    userId = targetUserId,
                    state = stateFilter ?: FriendshipState.NONE,
                    limit = limit,
                    offset = offset,
                )
            ) {
                is RepositoryResult.Success -> {
                    result.data
                }

                is RepositoryResult.Error -> {
                    return null
                }
            }

        val friendshipUsers =
            page.items.mapNotNull { friendship ->
                val otherUserId =
                    when (targetUserId) {
                        friendship.userLowId ->
                            friendship.userHighId

                        friendship.userHighId ->
                            friendship.userLowId

                        else ->
                            return@mapNotNull null
                    }

                val userRecord =
                    when (
                        val result =
                            userRepository.findById(
                                id = otherUserId,
                            )
                    ) {
                        is RepositoryResult.Success -> {
                            result.data
                        }

                        is RepositoryResult.Error -> {
                            return@mapNotNull null
                        }
                    }

                val profileImage =
                    userRecord.profileImageId
                        ?.let { profileImageId ->
                            when (
                                val result =
                                    mediaAssetRepository.findById(
                                        id = profileImageId,
                                    )
                            ) {
                                is RepositoryResult.Success -> {
                                    val media =
                                        result.data

                                    if (
                                        media.deletedAt != null
                                    ) {
                                        null
                                    } else {
                                        eu.vitamo.app.api.contracts.media.MediaReference(
                                            id = media.id,
                                            contentPath =
                                                "/media/${media.id}/content",
                                        )
                                    }
                                }

                                is RepositoryResult.Error -> {
                                    null
                                }
                            }
                        }

                val context =
                    friendship.toFriendshipContext(
                        currentUserId =
                            targetUserId,
                    )

                FriendshipUser(
                    user = userRecord.toUser(
                        accessLevel =
                            UserAccessLevel.PUBLIC,
                        profileImage =
                            profileImage,
                    ),
                    friendshipId =
                        friendship.id,
                    state =
                        context.state,
                )
            }

        return page.toPagedResult(mappedItems = friendshipUsers)
    }
}