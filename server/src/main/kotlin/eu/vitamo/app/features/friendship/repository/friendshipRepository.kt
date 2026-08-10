package eu.vitamo.app.features.friendship.repository

import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface FriendshipRepository {

    suspend fun findById(
        id: Uuid,
    ): RepositoryResult<FriendshipRecord>

    suspend fun findBetweenUsers(
        firstUserId: Uuid,
        secondUserId: Uuid,
    ): RepositoryResult<FriendshipRecord?>

    suspend fun findBetweenUserAndTargets(
        currentUserId: Uuid,
        targetUserIds: Collection<Uuid>,
    ): RepositoryResult<Map<Uuid, FriendshipRecord>>

    suspend fun createRequest(
        requesterId: Uuid,
        targetUserId: Uuid,
    ): RepositoryResult<FriendshipRecord>

    suspend fun acceptRequest(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord>

    suspend fun deletePendingRequest(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord>

    suspend fun deleteFriendship(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord>

    suspend fun findIncomingRequests(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>>

    suspend fun findOutgoingRequests(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>>

    suspend fun findFriends(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>>
}