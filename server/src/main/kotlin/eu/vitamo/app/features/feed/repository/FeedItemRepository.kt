package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.features.feed.model.PostRecord
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface FeedItemRepository {

    suspend fun createPost(
        authorId: Uuid,
        title: String?,
        messageJson: String?,
        createdAt: Instant,
    ): RepositoryResult<PostRecord>

    suspend fun findById(
        feedItemId: Uuid,
    ): RepositoryResult<PostRecord>

    suspend fun findFeed(
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<PostRecord>>

    suspend fun findFeedForUser(
        currentUserId: Uuid,
        friendIds: List<Uuid>,
        limit: Int,
        offset: Long
    ): RepositoryResult<Page<PostRecord>>


    suspend fun updatePost(
        feedItemId: Uuid,
        title: String?,
        messageJson: String?,
        updatedAt: Instant,
    ): RepositoryResult<PostRecord>

    suspend fun delete(
        feedItemId: Uuid,
        deletedAt: Instant,
    ): RepositoryResult<Unit>

    suspend fun like(
        feedItemId: Uuid,
        userId: Uuid,
        createdAt: Instant,
    ): RepositoryResult<Unit>

    suspend fun unlike(
        feedItemId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Unit>

    suspend fun findLikeUserIds(
        feedItemId: Uuid,
        limit: Int,
        offset: Long,
    ): RepositoryResult<Page<Uuid>>

    suspend fun hasLiked(
        feedItemId: Uuid,
        userId: Uuid,
    ): RepositoryResult<Boolean>

    suspend fun findLikedFeedItemIds(
        userId: Uuid,
        feedItemIds: Collection<Uuid>,
    ): RepositoryResult<Set<Uuid>>
}