package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface FeedItemReactionRepository {

    suspend fun createComment(
        feedItemId: Uuid,
        authorId: Uuid,
        contentJson: String,
        createdAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord>

    suspend fun createReply(
        feedItemId: Uuid,
        parentReactionId: Uuid,
        authorId: Uuid,
        contentJson: String,
        createdAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord>

    suspend fun findById(
        reactionId: Uuid,
    ): RepositoryResult<FeedItemReactionRecord>

    suspend fun findComments(
        feedItemId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FeedItemReactionRecord>>

    suspend fun findReplies(
        parentReactionId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<FeedItemReactionRecord>>

    suspend fun update(
        reactionId: Uuid,
        contentJson: String,
        updatedAt: Instant,
    ): RepositoryResult<FeedItemReactionRecord>

    suspend fun delete(
        reactionId: Uuid,
        deletedAt: Instant,
    ): RepositoryResult<Unit>

    suspend fun like(
        reactionId: Uuid,
        userId: Uuid,
        createdAt: Instant,
    ): RepositoryResult<Unit>

    suspend fun unlike(
        reactionId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Unit>

    suspend fun findLikeUserIds(
        reactionId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<Uuid>>

    suspend fun hasLiked(
        reactionId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Boolean>

    suspend fun findLikedReactionIds(
        userId: Uuid,
        reactionIds: Collection<Uuid>,
    ): RepositoryResult<Set<Uuid>>

    suspend fun findByFeedItemId(
        feedItemId: Uuid,
        parentReactionId: Uuid? = null,
        limit: Int = 20,
        offset: Long = 0L,
    ): RepositoryResult<Page<FeedItemReactionRecord>>
}