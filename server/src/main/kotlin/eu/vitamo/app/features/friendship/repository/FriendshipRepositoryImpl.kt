package eu.vitamo.app.features.friendship.repository


import com.google.cloud.firestore.Filter.and
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.friendship.error.FriendshipRepositoryErrors
import eu.vitamo.app.features.friendship.mapper.toFriendshipRecord
import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.features.friendship.model.FriendshipStatus
import eu.vitamo.app.features.friendship.model.canonicalUserPair
import eu.vitamo.app.features.friendship.table.FriendshipsTable
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid

class FriendshipRepositoryImpl :
    FriendshipRepository {

    override suspend fun findById(
        id: Uuid,
    ): RepositoryResult<FriendshipRecord> = dbQuery {
        val friendship = FriendshipsTable
            .selectAll()
            .where {
                FriendshipsTable.id eq id
            }
            .singleOrNull()
            ?.toFriendshipRecord()
            ?: return@dbQuery RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .friendshipNotFound(),
            )

        RepositoryResult.Success(
            data = friendship,
        )
    }

    override suspend fun findBetweenUsers(
        firstUserId: Uuid,
        secondUserId: Uuid,
    ): RepositoryResult<FriendshipRecord?> =
        dbQuery {
            if (firstUserId == secondUserId) {
                return@dbQuery RepositoryResult.Success(
                    data = null,
                )
            }

            val pair = canonicalUserPair(
                first = firstUserId,
                second = secondUserId,
            )

            val friendship = FriendshipsTable
                .selectAll()
                .where {
                    pairPredicate(
                        low = pair.low,
                        high = pair.high,
                    )
                }
                .singleOrNull()
                ?.toFriendshipRecord()

            RepositoryResult.Success(
                data = friendship,
            )
        }

    override suspend fun findBetweenUserAndTargets(
        currentUserId: Uuid,
        targetUserIds: Collection<Uuid>,
    ): RepositoryResult<Map<Uuid, FriendshipRecord>> =
        dbQuery {
            val targets = targetUserIds
                .filter { it != currentUserId }
                .distinct()

            if (targets.isEmpty()) {
                return@dbQuery RepositoryResult.Success(
                    data = emptyMap(),
                )
            }

            val records = FriendshipsTable
                .selectAll()
                .where {
                    (
                            (
                                    FriendshipsTable.userLowId eq
                                            currentUserId
                                    ) and
                                    (
                                            FriendshipsTable.userHighId inList
                                                    targets
                                            )
                            ) or
                            (
                                    (
                                            FriendshipsTable.userHighId eq
                                                    currentUserId
                                            ) and
                                            (
                                                    FriendshipsTable.userLowId inList
                                                            targets
                                                    )
                                    )
                }
                .map {
                    it.toFriendshipRecord()
                }

            RepositoryResult.Success(
                data = records.associateBy { friendship ->
                    friendship.otherUserId(
                        currentUserId = currentUserId,
                    )
                },
            )
        }

    override suspend fun createRequest(
        requesterId: Uuid,
        targetUserId: Uuid,
    ): RepositoryResult<FriendshipRecord> = dbQuery {
        if (requesterId == targetUserId) {
            return@dbQuery RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .cannotFriendSelf(),
            )
        }

        val pair = canonicalUserPair(
            first = requesterId,
            second = targetUserId,
        )

        val existing = FriendshipsTable
            .selectAll()
            .where {
                pairPredicate(
                    low = pair.low,
                    high = pair.high,
                )
            }
            .singleOrNull()
            ?.toFriendshipRecord()

        if (existing != null) {
            val error = when {
                existing.status ==
                        FriendshipStatus.ACCEPTED -> {
                    FriendshipRepositoryErrors
                        .alreadyFriends()
                }

                existing.requestedById ==
                        requesterId -> {
                    FriendshipRepositoryErrors
                        .friendRequestAlreadyExists()
                }

                else -> {
                    FriendshipRepositoryErrors
                        .incomingFriendRequestExists()
                }
            }

            return@dbQuery RepositoryResult.Error(
                error = error,
            )
        }

        val friendshipId = Uuid.random()
        val now = Clock.System.now()

        FriendshipsTable.insert {
            it[id] = friendshipId
            it[userLowId] = pair.low
            it[userHighId] = pair.high
            it[requestedById] = requesterId
            it[status] = FriendshipStatus.PENDING
            it[createdAt] = now
            it[updatedAt] = now
            it[acceptedAt] = null
        }

        val created = FriendshipsTable
            .selectAll()
            .where {
                FriendshipsTable.id eq
                        friendshipId
            }
            .single()
            .toFriendshipRecord()

        RepositoryResult.Success(
            data = created,
        )
    }

    override suspend fun acceptRequest(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord> = dbQuery {
        val friendship = findRecord(
            friendshipId = friendshipId,
        ) ?: return@dbQuery RepositoryResult.Error(
            error =
                FriendshipRepositoryErrors
                    .friendRequestNotFound(),
        )

        if (
            friendship.status !=
            FriendshipStatus.PENDING
        ) {
            return@dbQuery RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .friendRequestNotPending(),
            )
        }

        if (
            !friendship.containsUser(
                userId = currentUserId,
            ) ||
            friendship.requestedById ==
            currentUserId
        ) {
            return@dbQuery RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .notFriendRequestRecipient(),
            )
        }

        val now = Clock.System.now()

        val updatedRows = FriendshipsTable.update(
            where = {
                (
                        FriendshipsTable.id eq
                                friendshipId
                        ) and
                        (
                                FriendshipsTable.status eq
                                        FriendshipStatus.PENDING
                                )
            },
        ) {
            it[status] =
                FriendshipStatus.ACCEPTED

            it[acceptedAt] =
                now

            it[updatedAt] =
                now
        }

        if (updatedRows == 0) {
            return@dbQuery RepositoryResult.Error(
                error =
                    FriendshipRepositoryErrors
                        .friendRequestNotPending(),
            )
        }

        val updatedFriendship = findRecord(
            friendshipId = friendshipId,
        ) ?: return@dbQuery RepositoryResult.Error(
            error =
                FriendshipRepositoryErrors
                    .friendshipNotFound(),
        )

        RepositoryResult.Success(
            data = updatedFriendship,
        )
    }

    override suspend fun deletePendingRequest(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord> {
        return deleteByStatus(
            friendshipId = friendshipId,
            currentUserId = currentUserId,
            requiredStatus = FriendshipStatus.PENDING,
            notFoundError = FriendshipRepositoryErrors
                .friendRequestNotFound(),
            invalidStatusError = FriendshipRepositoryErrors
                .friendRequestNotPending(),
        )
    }

    override suspend fun deleteFriendship(
        friendshipId: Uuid,
        currentUserId: Uuid,
    ): RepositoryResult<FriendshipRecord> {
        return deleteByStatus(
            friendshipId = friendshipId,
            currentUserId = currentUserId,
            requiredStatus = FriendshipStatus.ACCEPTED,
            notFoundError = FriendshipRepositoryErrors
                .friendshipNotFound(),
            invalidStatusError = FriendshipRepositoryErrors
                .friendshipNotFound(),
        )
    }

    override suspend fun findIncomingRequests(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>> =
        dbQuery {
            findPage(
                predicate = {
                    (
                            FriendshipsTable.status eq
                                    FriendshipStatus.PENDING
                            ) and
                            (
                                    (
                                            FriendshipsTable.userLowId eq
                                                    currentUserId
                                            ) or
                                            (
                                                    FriendshipsTable.userHighId eq
                                                            currentUserId
                                                    )
                                    ) and
                            (
                                    FriendshipsTable.requestedById neq
                                            currentUserId
                                    )
                },
                limit = limit,
                offset = offset,
            )
        }

    override suspend fun findOutgoingRequests(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>> =
        dbQuery {
            findPage(
                predicate = {
                    (
                            FriendshipsTable.status eq
                                    FriendshipStatus.PENDING
                            ) and
                            (
                                    FriendshipsTable.requestedById eq
                                            currentUserId
                                    )
                },
                limit = limit,
                offset = offset,
            )
        }

    override suspend fun findFriendshipByState(
        userId: Uuid,
        limit: Int,
        offset: Long,
        state: FriendshipState,
    ): RepositoryResult<Page<FriendshipRecord>> = dbQuery {
        val participantPredicate =
            (FriendshipsTable.userHighId eq userId) or
                    (FriendshipsTable.userLowId eq userId)

        val statePredicate = when (state) {
                FriendshipState.NONE -> { FriendshipsTable.status eq FriendshipStatus.PENDING }
                FriendshipState.OUTGOING_REQUEST -> {
                    (FriendshipsTable.status eq
                            FriendshipStatus.PENDING) and
                            (FriendshipsTable.requestedById eq userId)
                }
                FriendshipState.INCOMING_REQUEST -> {
                    (FriendshipsTable.status eq
                            FriendshipStatus.PENDING) and
                            (FriendshipsTable.requestedById neq userId)
                }
                FriendshipState.FRIENDS -> {
                    FriendshipsTable.status eq
                            FriendshipStatus.ACCEPTED
                }
            }

        findPage(
            predicate = { participantPredicate and statePredicate },
            limit = limit,
            offset = offset,
        )
    }

    override suspend fun findFriends(
        currentUserId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>> =
        dbQuery {
            findPage(
                predicate = {
                    (
                            FriendshipsTable.status eq
                                    FriendshipStatus.ACCEPTED
                            ) and
                            (
                                    (
                                            FriendshipsTable.userLowId eq
                                                    currentUserId
                                            ) or
                                            (
                                                    FriendshipsTable.userHighId eq
                                                            currentUserId
                                                    )
                                    )
                },
                limit = limit,
                offset = offset,
            )
        }

    override suspend fun findFriendIds(currentUserId: Uuid): RepositoryResult<List<Uuid>> = dbQuery {
        val friendshipIds = FriendshipsTable.select(
            FriendshipsTable.userLowId,
            FriendshipsTable.userHighId
        )
            .where{
                predicate(currentUserId) and(FriendshipsTable.status eq FriendshipStatus.ACCEPTED)
            }
            .map { row ->
                val lowId = row[FriendshipsTable.userLowId]
                val highId = row[FriendshipsTable.userHighId]

                if (lowId == currentUserId) { highId } else { lowId }
            }

        RepositoryResult.Success(
            friendshipIds
        )
    }

    private fun findRecord(
        friendshipId: Uuid,
    ): FriendshipRecord? {
        return FriendshipsTable
            .selectAll()
            .where {
                FriendshipsTable.id eq
                        friendshipId
            }
            .singleOrNull()
            ?.toFriendshipRecord()
    }

    private fun findPage(
        predicate: () -> Op<Boolean>,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FriendshipRecord>> {
        val safeLimit = limit.coerceIn(
            minimumValue = 1,
            maximumValue = 50,
        )

        val safeOffset =
            offset.coerceAtLeast(0L)

        val total = FriendshipsTable
            .selectAll()
            .where {
                predicate()
            }
            .count()

        val items = FriendshipsTable
            .selectAll()
            .where {
                predicate()
            }
            .orderBy(
                FriendshipsTable.createdAt to
                        SortOrder.DESC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map {
                it.toFriendshipRecord()
            }

        return RepositoryResult.Success(
            data = Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    private fun pairPredicate(
        low: Uuid,
        high: Uuid,
    ): Op<Boolean> {
        return (
                FriendshipsTable.userLowId eq low
                ) and
                (
                        FriendshipsTable.userHighId eq high
                        )
    }

    private fun predicate(id: Uuid): Op<Boolean> =(FriendshipsTable.userHighId eq id) or (FriendshipsTable.userLowId eq id)


    private suspend fun deleteByStatus(
        friendshipId: Uuid,
        currentUserId: Uuid,
        requiredStatus: FriendshipStatus,
        notFoundError: RepositoryError,
        invalidStatusError: RepositoryError,
    ): RepositoryResult<FriendshipRecord> = dbQuery {
        val friendship = findRecord(
            friendshipId = friendshipId,
        ) ?: return@dbQuery RepositoryResult.Error(
            error = notFoundError,
        )

        if (friendship.status != requiredStatus) {
            return@dbQuery RepositoryResult.Error(
                error = invalidStatusError,
            )
        }

        if (!friendship.containsUser(currentUserId)) {
            return@dbQuery RepositoryResult.Error(
                error = FriendshipRepositoryErrors
                    .notFriendshipParticipant(),
            )
        }

        val deletedRows = FriendshipsTable.deleteWhere {
            (FriendshipsTable.id eq friendshipId) and
                    (FriendshipsTable.status eq requiredStatus)
        }

        if (deletedRows == 0) {
            return@dbQuery RepositoryResult.Error(
                error = invalidStatusError,
            )
        }

        RepositoryResult.Success(
            data = friendship,
        )
    }
}