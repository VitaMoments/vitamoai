package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.media.MediaType
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.media.entity.MediaAssetEntity
import eu.vitamo.app.features.media.mapper.toRecord
import eu.vitamo.app.features.media.model.CreateMediaAssetInput
import eu.vitamo.app.features.media.model.MediaAssetRecord
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid

class MediaAssetRepositoryImpl : MediaAssetRepository {

    override suspend fun createPending(
        input: CreateMediaAssetInput,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val now = Clock.System.now()

        val entity = MediaAssetEntity.new {
            ownerId = input.ownerId
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

    override suspend fun markReady(
        id: Uuid,
    ): RepositoryResult<MediaAssetRecord> = dbQuery {
        val entity = MediaAssetEntity.findById(id)
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
        val entity = MediaAssetEntity.findById(id)
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        entity.status = MediaStatus.FAILED
        entity.updatedAt = Clock.System.now()

        RepositoryResult.Success(Unit)
    }

    private fun buildStorageKey(
        ownerId: Uuid,
        mediaId: Uuid,
        extension: String,
    ): String {
        return "users/$ownerId/profile/$mediaId/origional.$extension"
    }

    override suspend fun markDeleted(
        id: Uuid,
    ): RepositoryResult<Unit> = dbQuery {
        val entity = MediaAssetEntity.findById(id)
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )

        val now = Clock.System.now()

        entity.status =
            MediaStatus.DELETED

        entity.deletedAt =
            now

        entity.updatedAt =
            now

        RepositoryResult.Success(Unit)
    }
}