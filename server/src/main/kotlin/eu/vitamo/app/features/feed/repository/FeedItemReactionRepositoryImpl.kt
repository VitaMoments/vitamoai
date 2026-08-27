package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.feed.entity.FeedItemReactionEntity
import eu.vitamo.app.features.feed.entity.FeedItemReactionLikeEntity
import eu.vitamo.app.features.feed.mapper.toReactionRecord
import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import eu.vitamo.app.features.feed.table.FeedItemReactionLikeTable
import eu.vitamo.app.features.feed.table.FeedItemReactionTable
import eu.vitamo.app.features.feed.table.FeedItemTable
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FeedItemReactionRepositoryImpl :
    FeedItemReactionRepository {

    override suspend fun createComment(
        feedItemId: Uuid,
        authorId: Uuid,
        contentJson: String,
        createdAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord> =
        createReaction(
            feedItemId = feedItemId,
            parentReactionId = null,
            authorId = authorId,
            contentJson = contentJson,
            createdAt = createdAt,
        )

    override suspend fun createReply(
        feedItemId: Uuid,
        parentReactionId: Uuid,
        authorId: Uuid,
        contentJson: String,
        createdAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord> =
        createReaction(
            feedItemId = feedItemId,
            parentReactionId = parentReactionId,
            authorId = authorId,
            contentJson = contentJson,
            createdAt = createdAt,
        )

    override suspend fun findById(
        reactionId: Uuid,
    ): RepositoryResult<FeedItemReactionRecord> = dbQuery {
        val entity = FeedItemReactionEntity
            .findById(reactionId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item reaction not found",
                ),
            )

        RepositoryResult.Success(
            entity.toRecord(),
        )
    }

    override suspend fun findComments(
        feedItemId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FeedItemReactionRecord>> =
        findByFeedItemId(
            feedItemId = feedItemId,
            parentReactionId = null,
            limit = limit,
            offset = offset,
        )

    override suspend fun findReplies(
        parentReactionId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FeedItemReactionRecord>> = dbQuery {
        val safeLimit = limit.coerceIn(
            minimumValue = 1,
            maximumValue = MAX_PAGE_SIZE,
        )

        val safeOffset =
            offset.coerceAtLeast(0L)

        val predicate =
            (FeedItemReactionTable.parentReactionId eq parentReactionId) and
                    FeedItemReactionTable.deletedAt.isNull()

        val total = FeedItemReactionTable
            .selectAll()
            .where {
                predicate
            }
            .count()

        val items = FeedItemReactionTable
            .selectAll()
            .where {
                predicate
            }
            .orderBy(
                FeedItemReactionTable.createdAt to SortOrder.ASC,
                FeedItemReactionTable.id to SortOrder.ASC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map { row ->
                row.toReactionRecord()
            }

        RepositoryResult.Success(
            Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    override suspend fun findByFeedItemId(
        feedItemId: Uuid,
        parentReactionId: Uuid?,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FeedItemReactionRecord>> = dbQuery {
        val safeLimit = limit.coerceIn(
            minimumValue = 1,
            maximumValue = MAX_PAGE_SIZE,
        )

        val safeOffset =
            offset.coerceAtLeast(0L)

        val feedItemEntityId = EntityID(
            id = feedItemId,
            table = FeedItemTable,
        )

        val parentPredicate =
            if (parentReactionId == null) {
                FeedItemReactionTable
                    .parentReactionId
                    .isNull()
            } else {
                FeedItemReactionTable.parentReactionId eq
                        parentReactionId
            }

        val predicate =
            (FeedItemReactionTable.feedItemId eq feedItemEntityId) and
                    parentPredicate and
                    FeedItemReactionTable.deletedAt.isNull()

        val total = FeedItemReactionTable
            .selectAll()
            .where {
                predicate
            }
            .count()

        val items = FeedItemReactionTable
            .selectAll()
            .where {
                predicate
            }
            .orderBy(
                FeedItemReactionTable.createdAt to SortOrder.ASC,
                FeedItemReactionTable.id to SortOrder.ASC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map { row ->
                row.toReactionRecord()
            }

        RepositoryResult.Success(
            Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    override suspend fun update(
        reactionId: Uuid,
        contentJson: String,
        updatedAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord> = dbQuery {
        val entity = FeedItemReactionEntity
            .findById(reactionId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item reaction not found",
                ),
            )

        entity.contentJson =
            contentJson

        entity.updatedAt =
            updatedAt.epochSeconds

        RepositoryResult.Success(
            entity.toRecord(),
        )
    }

    override suspend fun delete(
        reactionId: Uuid,
        deletedAt: Instant,
    ): RepositoryResult<Unit> = dbQuery {
        val entity = FeedItemReactionEntity
            .findById(reactionId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item reaction not found",
                ),
            )

        entity.deletedAt =
            deletedAt.epochSeconds

        entity.updatedAt =
            deletedAt.epochSeconds

        RepositoryResult.Success(Unit)
    }

    override suspend fun like(
        reactionId: Uuid,
        userId: Uuid,
        createdAt: Instant,
    ): RepositoryResult<Unit> = dbQuery {
        val reaction = FeedItemReactionEntity
            .findById(reactionId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item reaction not found",
                ),
            )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val exists =
            FeedItemReactionLikeEntity
                .find {
                    (
                            FeedItemReactionLikeTable.reactionId eq
                                    reaction.id
                            ) and
                            (
                                    FeedItemReactionLikeTable.userId eq
                                            userEntityId
                                    )
                }
                .firstOrNull() != null

        if (!exists) {
            FeedItemReactionLikeEntity.new {
                this.reactionId =
                    reaction.id

                this.userId =
                    userEntityId

                this.createdAt =
                    createdAt.epochSeconds
            }
        }

        RepositoryResult.Success(Unit)
    }

    override suspend fun unlike(
        reactionId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Unit> = dbQuery {

        val reaction =
            FeedItemReactionEntity.findById(
                id = reactionId,
            )
                ?.takeIf {
                    it.deletedAt == null
                }
                ?: return@dbQuery RepositoryResult.Error(
                    RepositoryError.NotFound(
                        message = "Feed item reaction not found",
                    ),
                )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        FeedItemReactionLikeEntity
            .find {
                (
                        FeedItemReactionLikeTable.reactionId eq
                                reaction.id
                        ) and
                        (
                                FeedItemReactionLikeTable.userId eq
                                        userEntityId
                                )
            }
            .firstOrNull()
            ?.delete()

        RepositoryResult.Success(Unit)
    }

    override suspend fun findLikeUserIds(
        reactionId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<Uuid>> = dbQuery {
        val safeLimit = limit.coerceIn(
            minimumValue = 1,
            maximumValue = MAX_PAGE_SIZE,
        )

        val safeOffset =
            offset.coerceAtLeast(0L)

        val reactionEntityId = EntityID(
            id = reactionId,
            table = FeedItemReactionTable,
        )

        val predicate =
            FeedItemReactionLikeTable.reactionId eq
                    reactionEntityId

        val total = FeedItemReactionLikeTable
            .selectAll()
            .where {
                predicate
            }
            .count()

        val items = FeedItemReactionLikeTable
            .selectAll()
            .where {
                predicate
            }
            .orderBy(
                FeedItemReactionLikeTable.createdAt to
                        SortOrder.DESC,
                FeedItemReactionLikeTable.id to
                        SortOrder.DESC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map { row ->
                row[
                    FeedItemReactionLikeTable.userId
                ].value
            }

        RepositoryResult.Success(
            Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    override suspend fun hasLiked(
        reactionId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Boolean> = dbQuery {
        val reactionEntityId = EntityID(
            id = reactionId,
            table = FeedItemReactionTable,
        )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val liked = FeedItemReactionLikeTable
            .selectAll()
            .where {
                (
                        FeedItemReactionLikeTable.reactionId eq
                                reactionEntityId
                        ) and
                        (
                                FeedItemReactionLikeTable.userId eq
                                        userEntityId
                                )
            }
            .limit(1)
            .count() > 0L

        RepositoryResult.Success(
            liked,
        )
    }

    override suspend fun findLikedReactionIds(
        userId: Uuid,
        reactionIds: Collection<Uuid>,
    ): RepositoryResult<Set<Uuid>> = dbQuery {
        if (reactionIds.isEmpty()) {
            return@dbQuery RepositoryResult.Success(
                data = emptySet(),
            )
        }

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val reactionEntityIds = reactionIds
            .distinct()
            .map { reactionId ->
                EntityID(
                    id = reactionId,
                    table = FeedItemReactionTable,
                )
            }

        val likedReactionIds =
            FeedItemReactionLikeTable
                .selectAll()
                .where {
                    (
                            FeedItemReactionLikeTable.userId eq
                                    userEntityId
                            ) and
                            (
                                    FeedItemReactionLikeTable.reactionId inList
                                            reactionEntityIds
                                    )
                }
                .mapTo(
                    mutableSetOf(),
                ) { row ->
                    row[
                        FeedItemReactionLikeTable.reactionId
                    ].value
                }

        RepositoryResult.Success(
            data = likedReactionIds,
        )
    }

    private suspend fun createReaction(
        feedItemId: Uuid,
        parentReactionId: Uuid?,
        authorId: Uuid,
        contentJson: String,
        createdAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord> = dbQuery {
        val feedItemEntityId = EntityID(
            id = feedItemId,
            table = FeedItemTable,
        )

        val authorEntityId = EntityID(
            id = authorId,
            table = UsersTable,
        )

        val entity =
            FeedItemReactionEntity.new {
                this.feedItemId =
                    feedItemEntityId

                this.parentReactionId =
                    parentReactionId

                this.authorId =
                    authorEntityId

                this.contentJson =
                    contentJson

                this.createdAt =
                    createdAt.epochSeconds

                this.updatedAt =
                    createdAt.epochSeconds

                this.deletedAt =
                    null
            }

        RepositoryResult.Success(
            entity.toRecord(),
        )
    }

    private fun FeedItemReactionEntity.toRecord() =
        FeedItemReactionRecord(
            id = id.value,
            feedItemId = feedItemId.value,
            parentReactionId = parentReactionId,
            authorId = authorId.value,
            contentJson = contentJson,
            createdAt = Instant.fromEpochSeconds(
                createdAt,
            ),
            updatedAt = Instant.fromEpochSeconds(
                updatedAt,
            ),
            deletedAt = deletedAt
                ?.let(
                    Instant::fromEpochSeconds,
                ),
        )

    private companion object {
        const val MAX_PAGE_SIZE = 50
    }
}