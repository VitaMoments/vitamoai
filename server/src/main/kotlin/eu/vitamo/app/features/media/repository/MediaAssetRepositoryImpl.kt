package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.media.MediaType
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.feed.entity.FeedItemEntity
import eu.vitamo.app.features.media.entity.MediaAssetEntity
import eu.vitamo.app.features.media.mapper.toRecord
import eu.vitamo.app.features.media.model.CreateMediaAssetInput
import eu.vitamo.app.features.media.model.MediaAssetRecord
import eu.vitamo.app.features.media.table.MediaAssetsTable
import eu.vitamo.app.repository.FieldError
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

class MediaAssetRepositoryImpl : MediaAssetRepository {

    override suspend fun createPending(
        input: CreateMediaAssetInput,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val now = Clock.System.now()

        val entity = MediaAssetEntity.new {
            ownerId = input.ownerId

            feedItemId = null
            position = null

            mediaType = MediaType.IMAGE
            purpose = input.purpose
            status = MediaStatus.PENDING
            visibility = input.visibility

            storageKey = ""

            mimeType = input.mimeType
            sizeBytes = input.sizeBytes
            width = input.width
            height = input.height
            sha256 = input.sha256

            createdAt = now
            updatedAt = now
            deletedAt = null
        }

        entity.storageKey = buildStorageKey(
            ownerId = input.ownerId,
            mediaId = entity.id.value,
            purpose = input.purpose,
            extension = input.extension,
        )

        RepositoryResult.Success(
            data = entity.toRecord(),
        )
    }

    override suspend fun findById(
        id: Uuid,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val record = MediaAssetEntity
            .findById(id)
            ?.takeIf {
                it.deletedAt == null
            }
            ?.toRecord()
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        RepositoryResult.Success(
            data = record,
        )
    }

    override suspend fun findByIds(
        ids: Collection<Uuid>,
    ): RepositoryResult<List<MediaAssetRecord>> = dbQuery {
        if (ids.isEmpty()) {
            return@dbQuery RepositoryResult.Success(
                data = emptyList(),
            )
        }

        val entityIds = ids
            .distinct()
            .map { mediaId ->
                EntityID(
                    id = mediaId,
                    table = MediaAssetsTable,
                )
            }

        val records = MediaAssetEntity
            .find {
                (MediaAssetsTable.id inList entityIds) and
                        MediaAssetsTable.deletedAt.isNull()
            }
            .map {
                it.toRecord()
            }

        RepositoryResult.Success(
            data = records,
        )
    }

    override suspend fun findByFeedItemId(
        feedItemId: Uuid,
    ): RepositoryResult<List<MediaAssetRecord>> = dbQuery {
        val feedItem = FeedItemEntity
            .findById(feedItemId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Feed item was not found.",
                ),
            )

        val records = MediaAssetEntity
            .find {
                (MediaAssetsTable.feedItemId eq feedItem.id) and
                        MediaAssetsTable.deletedAt.isNull()
            }
            .orderBy(
                MediaAssetsTable.position to
                        SortOrder.ASC,
            )
            .map {
                it.toRecord()
            }

        RepositoryResult.Success(
            data = records,
        )
    }

    override suspend fun attachToFeedItem(
        mediaId: Uuid,
        feedItemId: Uuid,
        position: Int,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        if (position < 0) {
            return@dbQuery RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "position",
                            message =
                                "Media position cannot be negative.",
                        ),
                    ),
                    message = "Invalid media position.",
                ),
            )
        }

        val feedItem = findFeedItemEntity(
            feedItemId = feedItemId,
        )
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Feed item was not found.",
                ),
            )

        val media = MediaAssetEntity
            .findById(mediaId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        val validationError = validateFeedAttachment(
            media = media,
            feedItem = feedItem,
            field = "mediaId",
        )

        if (validationError != null) {
            return@dbQuery RepositoryResult.Error(
                error = validationError,
            )
        }

        media.feedItemId = feedItem.id
        media.position = position
        media.updatedAt = Clock.System.now()

        RepositoryResult.Success(
            data = media.toRecord(),
        )
    }

    override suspend fun attachAllToFeedItem(
        mediaIds: List<Uuid>,
        feedItemId: Uuid,
    ): RepositoryResult<List<MediaAssetRecord>> = dbQuery {
        if (mediaIds.isEmpty()) {
            return@dbQuery RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "mediaIds",
                            message =
                                "At least one media asset is required.",
                        ),
                    ),
                    message = "Media is required.",
                ),
            )
        }

        if (mediaIds.distinct().size != mediaIds.size) {
            return@dbQuery RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "mediaIds",
                            message =
                                "Duplicate media assets are not allowed.",
                        ),
                    ),
                    message = "Invalid media.",
                ),
            )
        }

        val feedItem = findFeedItemEntity(
            feedItemId = feedItemId,
        )
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Feed item was not found.",
                ),
            )

        val entityIds = mediaIds.map { mediaId ->
            EntityID(
                id = mediaId,
                table = MediaAssetsTable,
            )
        }

        val mediaById = MediaAssetEntity
            .find {
                (MediaAssetsTable.id inList entityIds) and
                        MediaAssetsTable.deletedAt.isNull()
            }
            .associateBy {
                it.id.value
            }

        if (mediaById.size != mediaIds.size) {
            return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message =
                        "One or more media assets were not found.",
                ),
            )
        }

        val orderedMedia = mediaIds.map { mediaId ->
            mediaById.getValue(mediaId)
        }

        /*
         * Eerst ALLES valideren.
         *
         * Pas als alle media geldig zijn worden records aangepast.
         * Hierdoor krijgen we geen gedeeltelijke koppeling binnen
         * deze database-transactie.
         */
        for ((index, media) in orderedMedia.withIndex()) {
            val validationError =
                validateFeedAttachment(
                    media = media,
                    feedItem = feedItem,
                    field = "mediaIds[$index]",
                )

            if (validationError != null) {
                return@dbQuery RepositoryResult.Error(
                    error = validationError,
                )
            }
        }

        val now = Clock.System.now()

        orderedMedia.forEachIndexed { index, media ->
            media.feedItemId = feedItem.id
            media.position = index
            media.updatedAt = now
        }

        RepositoryResult.Success(
            data = orderedMedia.map {
                it.toRecord()
            },
        )
    }

    override suspend fun detachFromFeedItem(
        mediaId: Uuid,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val media = MediaAssetEntity
            .findById(mediaId)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        media.feedItemId = null
        media.position = null
        media.updatedAt = Clock.System.now()

        RepositoryResult.Success(
            data = media.toRecord(),
        )
    }

    override suspend fun markReady(
        id: Uuid,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val entity = MediaAssetEntity
            .findById(id)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        entity.status = MediaStatus.READY
        entity.updatedAt = Clock.System.now()

        RepositoryResult.Success(
            data = entity.toRecord(),
        )
    }

    override suspend fun markFailed(
        id: Uuid,
    ): RepositoryResult<Unit> = dbQuery {
        val entity = MediaAssetEntity
            .findById(id)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        entity.status = MediaStatus.FAILED
        entity.updatedAt = Clock.System.now()

        RepositoryResult.Success(Unit)
    }

    override suspend fun markDeleted(
        id: Uuid,
    ): RepositoryResult<Unit> = dbQuery {
        val entity = MediaAssetEntity
            .findById(id)
            ?.takeIf {
                it.deletedAt == null
            }
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        val now = Clock.System.now()

        entity.status = MediaStatus.DELETED
        entity.deletedAt = now
        entity.updatedAt = now

        RepositoryResult.Success(Unit)
    }

    private fun findFeedItemEntity(
        feedItemId: Uuid,
    ): FeedItemEntity? =
        FeedItemEntity
            .findById(feedItemId)
            ?.takeIf {
                it.deletedAt == null
            }

    private fun validateFeedAttachment(
        media: MediaAssetEntity,
        feedItem: FeedItemEntity,
        field: String,
    ): RepositoryError? {
        /*
         * Een gebruiker mag alleen zijn eigen media
         * aan zijn eigen FeedItem koppelen.
         */
        if (
            media.ownerId !=
            feedItem.authorId.value
        ) {
            return RepositoryError.Forbidden(
                message =
                    "Media asset does not belong to the feed item author.",
            )
        }

        if (
            media.purpose !=
            MediaPurpose.FEED_ATTACHMENT
        ) {
            return RepositoryError.BadRequest(
                errors = listOf(
                    FieldError(
                        field = field,
                        message =
                            "Media asset is not a feed attachment.",
                    ),
                ),
                message = "Invalid media asset.",
            )
        }

        if (
            media.status !=
            MediaStatus.READY
        ) {
            return RepositoryError.BadRequest(
                errors = listOf(
                    FieldError(
                        field = field,
                        message =
                            "Media asset is not ready.",
                    ),
                ),
                message = "Invalid media asset.",
            )
        }

        if (
            media.feedItemId != null ||
            media.position != null
        ) {
            return RepositoryError.Conflict(
                errors = listOf(
                    FieldError(
                        field = field,
                        message =
                            "Media asset is already attached to a feed item.",
                    ),
                ),
                message =
                    "Media asset is already attached.",
            )
        }

        return null
    }

    private fun buildStorageKey(
        ownerId: Uuid,
        mediaId: Uuid,
        purpose: MediaPurpose,
        extension: String,
    ): String {
        val directory = when (purpose) {
            MediaPurpose.PROFILE_IMAGE ->
                "profile"

            MediaPurpose.FEED_ATTACHMENT ->
                "feed"
        }

        return "users/$ownerId/$directory/$mediaId/original.$extension"
    }
}