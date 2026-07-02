package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.model.FeedItemRecord
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface FeedRepository {
    suspend fun create(authorId: Uuid, request: CreateFeedItemRequest, now: Instant): FeedItemRecord
    suspend fun getById(uuid: Uuid): FeedItemRecord?
    suspend fun getForUser(authorId: Uuid, limit: Int, offset: Int): Pair<List<FeedItemRecord>, Long>
    suspend fun getAllVisibleCandidates(limit: Int, offset: Int, categories: Set<FeedCategory>): Pair<List<FeedItemRecord>, Long>
    suspend fun update(uuid: Uuid, authorId: Uuid, request: UpdateFeedItemRequest, now: Instant): FeedItemRecord?
    suspend fun softDelete(uuid: Uuid, authorId: Uuid, now: Instant): Boolean
}
