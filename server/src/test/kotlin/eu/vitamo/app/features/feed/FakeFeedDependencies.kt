package eu.vitamo.app.features.feed

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.MediaAsset
import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.api.contracts.feed.RichTextDocument
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.features.feed.model.FeedItemRecord
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.service.FeedFriendshipService
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserAccount
import eu.vitamo.app.features.user.repository.UserRepository
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FakeFeedRepository : FeedRepository {
    val items = mutableListOf<FeedItemRecord>()

    override suspend fun create(authorId: Uuid, request: CreateFeedItemRequest, now: Instant): FeedItemRecord {
        val item = FeedItemRecord(
            uuid = Uuid.random(),
            authorId = authorId,
            content = request.content,
            privacy = request.privacy,
            categories = request.categories,
            mediaAssets = request.mediaAssets,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
        items.add(item)
        return item
    }

    override suspend fun getById(uuid: Uuid): FeedItemRecord? = items.firstOrNull { it.uuid == uuid && it.deletedAt == null }

    override suspend fun getForUser(authorId: Uuid, limit: Int, offset: Int): Pair<List<FeedItemRecord>, Long> {
        val filtered = items.filter { it.authorId == authorId && it.deletedAt == null }
        return filtered.drop(offset).take(limit) to filtered.size.toLong()
    }

    override suspend fun getAllVisibleCandidates(limit: Int, offset: Int, categories: Set<FeedCategory>): Pair<List<FeedItemRecord>, Long> {
        val filtered = items.filter { it.deletedAt == null && (categories.isEmpty() || it.categories.any(categories::contains)) }
        return filtered.drop(offset).take(limit) to filtered.size.toLong()
    }

    override suspend fun update(uuid: Uuid, authorId: Uuid, request: UpdateFeedItemRequest, now: Instant): FeedItemRecord? {
        val current = items.firstOrNull { it.uuid == uuid && it.authorId == authorId && it.deletedAt == null } ?: return null
        val updated = current.copy(
            content = request.content ?: current.content,
            privacy = request.privacy ?: current.privacy,
            categories = request.categories ?: current.categories,
            mediaAssets = request.mediaAssets ?: current.mediaAssets,
            updatedAt = now,
        )
        items.remove(current)
        items.add(updated)
        return updated
    }

    override suspend fun softDelete(uuid: Uuid, authorId: Uuid, now: Instant): Boolean {
        val current = items.firstOrNull { it.uuid == uuid && it.authorId == authorId && it.deletedAt == null } ?: return false
        items.remove(current)
        items.add(current.copy(deletedAt = now, updatedAt = now))
        return true
    }
}

class FakeUserRepository : UserRepository {
    val user = UserAccount(
        id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
        email = "user@example.com",
        displayName = "User",
        hashedPassword = "hash",
        firstName = null,
        lastName = null,
        alias = null,
        bio = null,
        birthDate = null,
        role = UserRole.USER,
        emailVerifiedAt = Clock.System.now(),
        createdAt = 0,
        updatedAt = 0,
        deletedAt = null,
    )

    override suspend fun findByEmail(email: String): UserAccount? = user
    override suspend fun findByEmailAsEntity(email: String): UserEntity? = null
    override suspend fun findById(id: Uuid): UserAccount? = user.copy(id = id)
    override fun deleteById(id: Uuid) = Unit
    override suspend fun createUser(
        email: String,
        displayName: String,
        hashedPassword: String,
        firstName: String?,
        lastName: String?,
        alias: String?,
        birthDate: LocalDate?,
        now: Long,
    ): UserAccount = user
    override fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long) = Unit
    override fun updatePassword(userid: Uuid, hashedPassword: String) = Unit
}

class FakeFriendshipService(
    private val areFriends: Boolean = false,
) : FeedFriendshipService {
    override suspend fun areFriends(userA: Uuid, userB: Uuid): Boolean = areFriends
}

fun sampleCreateRequest(text: String = "hello", privacy: PrivacyStatus = PrivacyStatus.PUBLIC): CreateFeedItemRequest {
    return CreateFeedItemRequest(
        content = RichTextDocument(
            type = "markdown",
            content = buildJsonObject { put("text", text) },
        ),
        privacy = privacy,
        categories = emptyList(),
        mediaAssets = emptyList<MediaAsset>(),
    )
}
