package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.MediaAssetType
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.entity.FeedItemCategoryEntity
import eu.vitamo.app.features.feed.entity.FeedItemEntity
import eu.vitamo.app.features.feed.entity.FeedItemMediaAssetEntity
import eu.vitamo.app.features.feed.model.FeedItemRecord
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.serialization.AppJson
import eu.vitamo.app.database.helpers.dbQuery
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExposedFeedRepository : FeedRepository {
    override suspend fun create(authorId: Uuid, request: CreateFeedItemRequest, now: Instant): FeedItemRecord = dbQuery {
        val author = UserEntity.findById(authorId)
            ?: throw FeedException.NotFound("Author not found")

        val entity = FeedItemEntity.new {
            this.author = author
            this.contentJson = AppJson.encodeToString(request.content)
            this.privacy = request.privacy
            this.createdAt = now
            this.updatedAt = now
            this.deletedAt = null
        }

        replaceCategories(entity, request.categories)
        replaceMediaAssets(entity, request.mediaAssets)

        entity.toRecord()
    }

    override suspend fun getById(uuid: Uuid): FeedItemRecord? = dbQuery {
        FeedItemEntity.findById(uuid)
            ?.takeIf { it.deletedAt == null }
            ?.toRecord()
    }

    override suspend fun getForUser(authorId: Uuid, limit: Int, offset: Int): Pair<List<FeedItemRecord>, Long> = dbQuery {
        val all = FeedItemEntity.all()
            .filter { it.deletedAt == null && it.author.id.value == authorId }
            .sortedByDescending { it.createdAt }

        all.drop(offset).take(limit).map { it.toRecord() } to all.size.toLong()
    }

    override suspend fun getAllVisibleCandidates(
        limit: Int,
        offset: Int,
        categories: Set<FeedCategory>,
    ): Pair<List<FeedItemRecord>, Long> = dbQuery {
        val all = FeedItemEntity.all()
            .filter { it.deletedAt == null }
            .filter { entity ->
                categories.isEmpty() || entity.categories().any { it in categories }
            }
            .sortedByDescending { it.createdAt }

        all.drop(offset).take(limit).map { it.toRecord() } to all.size.toLong()
    }

    override suspend fun update(
        uuid: Uuid,
        authorId: Uuid,
        request: UpdateFeedItemRequest,
        now: Instant,
    ): FeedItemRecord? = dbQuery {
        val entity = FeedItemEntity.findById(uuid)
            ?.takeIf { it.deletedAt == null && it.author.id.value == authorId }
            ?: return@dbQuery null

        request.content?.let { entity.contentJson = AppJson.encodeToString(it) }
        request.privacy?.let { entity.privacy = it }
        request.categories?.let { replaceCategories(entity, it) }
        request.mediaAssets?.let { replaceMediaAssets(entity, it) }
        entity.updatedAt = now

        entity.toRecord()
    }

    override suspend fun softDelete(uuid: Uuid, authorId: Uuid, now: Instant): Boolean = dbQuery {
        val entity = FeedItemEntity.findById(uuid)
            ?.takeIf { it.deletedAt == null && it.author.id.value == authorId }
            ?: return@dbQuery false

        entity.deletedAt = now
        entity.updatedAt = now
        true
    }

    private fun replaceCategories(entity: FeedItemEntity, categories: List<FeedCategory>) {
        FeedItemCategoryEntity.find { eu.vitamo.app.features.feed.table.FeedItemCategoriesTable.feedItem eq entity.id }
            .forEach { it.delete() }
        categories.distinct().forEach { category ->
            FeedItemCategoryEntity.new {
                this.feedItem = entity
                this.category = category
            }
        }
    }

    private fun replaceMediaAssets(entity: FeedItemEntity, mediaAssets: List<eu.vitamo.app.api.contracts.feed.MediaAsset>) {
        FeedItemMediaAssetEntity.find { eu.vitamo.app.features.feed.table.FeedItemMediaAssetsTable.feedItem eq entity.id }
            .forEach { it.delete() }
        mediaAssets.distinctBy { it.id }.forEach { media ->
            FeedItemMediaAssetEntity.new {
                this.feedItem = entity
                this.mediaAssetId = media.id
                this.mediaAssetUrl = media.url
                this.mediaAssetType = media.type.name
                this.mediaAssetMetadata = media.metadata?.toString()
            }
        }
    }

    private fun FeedItemEntity.categories(): List<FeedCategory> {
        return FeedItemCategoryEntity.find { eu.vitamo.app.features.feed.table.FeedItemCategoriesTable.feedItem eq id }
            .map { it.category }
    }

    private fun FeedItemEntity.mediaAssets(): List<eu.vitamo.app.api.contracts.feed.MediaAsset> {
        return FeedItemMediaAssetEntity.find { eu.vitamo.app.features.feed.table.FeedItemMediaAssetsTable.feedItem eq id }
            .map {
                eu.vitamo.app.api.contracts.feed.MediaAsset(
                    id = it.mediaAssetId,
                    url = it.mediaAssetUrl,
                    type = MediaAssetType.valueOf(it.mediaAssetType),
                    metadata = it.mediaAssetMetadata?.let(AppJson::parseToJsonElement),
                )
            }
    }

    private fun FeedItemEntity.toRecord(): FeedItemRecord {
        return FeedItemRecord(
            uuid = id.value,
            authorId = author.id.value,
            content = AppJson.decodeFromString(contentJson),
            privacy = privacy,
            categories = categories(),
            mediaAssets = mediaAssets(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        )
    }
}
