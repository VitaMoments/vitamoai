package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.feed.entity.FeedItemEntity
import eu.vitamo.app.features.feed.entity.FeedItemLikeEntity
import eu.vitamo.app.features.feed.entity.PostEntity
import eu.vitamo.app.features.feed.mapper.toPostRecord
import eu.vitamo.app.features.feed.mapper.toReactionRecord
import eu.vitamo.app.features.feed.model.PostRecord
import eu.vitamo.app.features.feed.table.FeedItemLikeTable
import eu.vitamo.app.features.feed.table.FeedItemTable
import eu.vitamo.app.features.feed.table.PostTable
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

class FeedItemRepositoryImpl : FeedItemRepository {

    override suspend fun createPost(
        authorId: Uuid,
        title: String?,
        messageJson: String?,
        createdAt: Instant,
    ): RepositoryResult<PostRecord> = dbQuery {

        val feedItem = FeedItemEntity.new {
            this.authorId = EntityID(
                id = authorId,
                table = UsersTable,
            )
            this.type = POST_TYPE
            this.createdAt = createdAt.epochSeconds
            this.updatedAt = createdAt.epochSeconds
            this.deletedAt = null
        }

        val post = PostEntity.new {
            this.feedItemId = feedItem.id
            this.title = title
            this.messageJson = messageJson
        }

        RepositoryResult.Success(
            post.toRecord(
                feedItem = feedItem,
            ),
        )
    }

    override suspend fun findById(
        feedItemId: Uuid,
    ): RepositoryResult<PostRecord> = dbQuery {

        val feedItem = FeedItemEntity.findById(
            id = feedItemId,
        )
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item not found",
                ),
            )

        val post = PostEntity.find {
            PostTable.feedItemId eq feedItem.id
        }.firstOrNull()
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Post not found",
                ),
            )

        RepositoryResult.Success(
            post.toRecord(
                feedItem = feedItem,
            ),
        )
    }

    override suspend fun findFeed(
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<PostRecord>> = dbQuery {
        val safeLimit = limit.coerceIn(
            minimumValue = 1,
            maximumValue = 50,
        )
        val safeOffset = offset.coerceAtLeast(0)

        val predicate =
            FeedItemTable.deletedAt.isNull() and
                (FeedItemTable.type eq POST_TYPE)

        val total = FeedItemTable
            .selectAll()
            .where {
                predicate
            }
            .count()

        val items =
            (FeedItemTable innerJoin PostTable)
                .selectAll()
                .where {
                    predicate
                }
                .orderBy(
                    FeedItemTable.createdAt to SortOrder.DESC,
                    FeedItemTable.id to SortOrder.DESC,
                )
                .limit(safeLimit)
                .offset(safeOffset)
                .map { row ->
                    PostRecord(
                        id = row[FeedItemTable.id].value,
                        authorId =
                            row[FeedItemTable.authorId].value,
                        title =
                            row[PostTable.title],
                        messageJson =
                            row[PostTable.messageJson],
                        createdAt = Instant.fromEpochSeconds(
                            row[FeedItemTable.createdAt],
                        ),
                        updatedAt = Instant.fromEpochSeconds(
                            row[FeedItemTable.updatedAt],
                        ),
                        deletedAt =
                            row[FeedItemTable.deletedAt]
                                ?.let(Instant::fromEpochSeconds),
                    )
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

    override suspend fun findFeedForUser(
        currentUserId: Uuid,
        friendIds: List<Uuid>,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<PostRecord>> = dbQuery {
        val safeLimit =
            limit.coerceIn(
                minimumValue = 1,
                maximumValue = 50,
            )

        val safeOffset =
            offset.coerceAtLeast(0L)

        val authorIds =
            (friendIds + currentUserId)
                .distinct()

        val predicate =
            FeedItemTable.deletedAt.isNull() and
                    (FeedItemTable.type eq POST_TYPE) and
                    (FeedItemTable.authorId inList authorIds)

        val feedQuery =
            (FeedItemTable innerJoin PostTable)
                .selectAll()
                .where {
                    predicate
                }

        val total =
            feedQuery.count()

        val items =
            (FeedItemTable innerJoin PostTable)
                .selectAll()
                .where {
                    predicate
                }
                .orderBy(
                    FeedItemTable.createdAt to SortOrder.DESC,
                    FeedItemTable.id to SortOrder.DESC,
                )
                .limit(
                    count = safeLimit,
                )
                .offset(
                    start = safeOffset,
                )
                .map { it.toPostRecord() }

        RepositoryResult.Success(
            Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    override suspend fun updatePost(
        feedItemId: Uuid,
        title: String?,
        messageJson: String?,
        updatedAt: Instant,
    ): RepositoryResult<PostRecord> = dbQuery {

        val feedItem = FeedItemEntity.findById(feedItemId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item not found",
                ),
            )

        val post = PostEntity.find {
            PostTable.feedItemId eq feedItem.id
        }.firstOrNull()
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Post not found",
                ),
            )

        post.title = title
        post.messageJson = messageJson

        feedItem.updatedAt = updatedAt.epochSeconds

        RepositoryResult.Success(
            post.toRecord(
                feedItem = feedItem,
            ),
        )
    }

    override suspend fun delete(
        feedItemId: Uuid,
        deletedAt: Instant,
    ): RepositoryResult<Unit> = dbQuery {

        val feedItem = FeedItemEntity.findById(feedItemId)
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item not found",
                ),
            )

        feedItem.deletedAt = deletedAt.epochSeconds
        feedItem.updatedAt = deletedAt.epochSeconds

        RepositoryResult.Success(Unit)
    }

    override suspend fun like(
        feedItemId: Uuid,
        userId: Uuid,
        createdAt: Instant,
    ): RepositoryResult<Unit> = dbQuery {

        val feedItem = FeedItemEntity.findById(feedItemId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item not found",
                ),
            )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val exists = FeedItemLikeEntity.find {
            (FeedItemLikeTable.feedItemId eq feedItem.id) and
                (FeedItemLikeTable.userId eq userEntityId)
        }.firstOrNull() != null

        if (!exists) {
            FeedItemLikeEntity.new {
                this.feedItemId = feedItem.id
                this.userId = userEntityId
                this.createdAt = createdAt.epochSeconds
            }
        }

        RepositoryResult.Success(Unit)
    }

    override suspend fun unlike(
        feedItemId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Unit> = dbQuery {

        val feedItem = FeedItemEntity.findById(
            id = feedItemId,
        )
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Feed item not found",
                ),
            )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        FeedItemLikeEntity.find {
            (FeedItemLikeTable.feedItemId eq feedItem.id) and
                    (FeedItemLikeTable.userId eq userEntityId)
        }
            .firstOrNull()
            ?.delete()

        RepositoryResult.Success(Unit)
    }

    override suspend fun findLikeUserIds(
        feedItemId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<Uuid>> = dbQuery {

        val safeLimit = limit.coerceIn(1, 50)
        val safeOffset = offset.coerceAtLeast(0)

        val feedItemEntityId = EntityID(
            id = feedItemId,
            table = FeedItemTable,
        )

        val predicate =
            FeedItemLikeTable.feedItemId eq feedItemEntityId

        val total = FeedItemLikeTable
            .selectAll()
            .where {
                predicate
            }
            .count()

        val items = FeedItemLikeTable
            .selectAll()
            .where {
                predicate
            }
            .orderBy(
                FeedItemLikeTable.createdAt to SortOrder.DESC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map { row ->
                row[FeedItemLikeTable.userId].value
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
        feedItemId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Boolean> = dbQuery {

        val feedItemEntityId = EntityID(
            id = feedItemId,
            table = FeedItemTable,
        )

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val liked = FeedItemLikeTable
            .selectAll()
            .where {
                (FeedItemLikeTable.feedItemId eq feedItemEntityId) and
                    (FeedItemLikeTable.userId eq userEntityId)
            }
            .limit(1)
            .count() > 0

        RepositoryResult.Success(liked)
    }

    override suspend fun findLikedFeedItemIds(
        userId: Uuid,
        feedItemIds: Collection<Uuid>,
    ): RepositoryResult<Set<Uuid>> = dbQuery {
        if (feedItemIds.isEmpty()) {
            return@dbQuery RepositoryResult.Success(
                data = emptySet(),
            )
        }

        val userEntityId = EntityID(
            id = userId,
            table = UsersTable,
        )

        val feedItemEntityIds = feedItemIds
            .distinct()
            .map { feedItemId ->
                EntityID(
                    id = feedItemId,
                    table = FeedItemTable,
                )
            }

        val likedFeedItemIds = FeedItemLikeTable
            .selectAll()
            .where {
                (FeedItemLikeTable.userId eq userEntityId) and
                        (
                                FeedItemLikeTable.feedItemId inList
                                        feedItemEntityIds
                                )
            }
            .mapTo(mutableSetOf()) { row ->
                row[FeedItemLikeTable.feedItemId].value
            }

        RepositoryResult.Success(
            data = likedFeedItemIds,
        )
    }

    private fun PostEntity.toRecord(
        feedItem: FeedItemEntity,
    ): PostRecord =
        PostRecord(
            id = feedItem.id.value,
            authorId = feedItem.authorId.value,
            title = title,
            messageJson = messageJson,
            createdAt = Instant.fromEpochSeconds(
                feedItem.createdAt,
            ),
            updatedAt = Instant.fromEpochSeconds(
                feedItem.updatedAt,
            ),
            deletedAt = feedItem.deletedAt
                ?.let(Instant::fromEpochSeconds),
        )

    private companion object {
        const val POST_TYPE = "POST"
    }
}