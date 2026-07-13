package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaAssetContext
import eu.vitamo.app.api.contracts.media.MediaCreateInput
import eu.vitamo.app.api.contracts.media.MediaPurposeType
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.error.ErrorResponse
import eu.vitamo.app.features.media.entity.MediaAssetEntity
import eu.vitamo.app.features.media.mapper.toMediaAsset
import eu.vitamo.app.media.table.MediaAssetsTable
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class MediaRepositoryImpl : MediaRepository {
    override suspend fun create(input: MediaCreateInput): RepositoryResult<MediaAssetContext> = dbQuery {
        val entity = MediaAssetEntity.new(
            id = input.uuid
        ) {
            this.referenceId = input.referenceId
            this.referenceType = input.referenceType
            this.purpose = input.purpose
            this.privacy = input.privacy
            this.originalFileName = input.originalFileName
            this.storedFileName = input.storedFileName
            this.objectKey = input.objectKey
            this.contentType = input.contentType
            this.sizeBytes = input.sizeBytes
            this.width = input.width
            this.height = input.height
        }

        RepositoryResult.Success(entity.toMediaAsset())
    }

    override suspend fun findById(id: Uuid): RepositoryResult<MediaAssetContext> = dbQuery {
        val entity = MediaAssetEntity.findById(id)
            ?.takeIf { it.deletedAt == null }
            ?: throw ErrorResponse.NotFound(message = "No media found with id $id")
        RepositoryResult.Success(entity.toMediaAsset())
    }

    override suspend fun findAllByReference(
        referenceId: Uuid,
        referenceType: MediaReferenceType
    ): RepositoryResult<List<MediaAssetContext>> = dbQuery {
        val list = MediaAssetEntity.find {
            (MediaAssetsTable.referenceId eq referenceId) and
                    (MediaAssetsTable.referenceType eq referenceType) and
                    MediaAssetsTable.deletedAt.isNull()
        }
            .map { it.toMediaAsset() }
            .toList()

        RepositoryResult.Success(list)
    }

    override suspend fun findProfileImage(userId: Uuid): RepositoryResult<MediaAssetContext?> = dbQuery {
        val entity = MediaAssetEntity.find {
            (MediaAssetsTable.referenceId eq userId) and
                    (MediaAssetsTable.referenceType eq MediaReferenceType.USER) and
                    (MediaAssetsTable.purpose eq MediaPurposeType.PROFILE) and
                    MediaAssetsTable.deletedAt.isNull()
        }
            .orderBy(MediaAssetsTable.createdAt to SortOrder.DESC)
            .firstOrNull()
        RepositoryResult.Success(entity?.toMediaAsset())
    }

    override suspend fun findAllByReferenceAndPurpose(
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType
    ): RepositoryResult<List<MediaAssetContext>> = dbQuery {
        val list = MediaAssetEntity.find {
            (MediaAssetsTable.referenceId eq referenceId) and
                    (MediaAssetsTable.referenceType eq referenceType) and
                    (MediaAssetsTable.purpose eq purpose) and
                    MediaAssetsTable.deletedAt.isNull()
        }
            .map { it.toMediaAsset() }
            .toList()

        RepositoryResult.Success(list)
    }

    override suspend fun softDelete(
        id: Uuid,
        deletedBy: Uuid
    ): RepositoryResult<Boolean> = dbQuery {
        val entity = MediaAssetEntity.findById(id)
            ?.takeIf { it.deletedAt == null }
            ?: throw ErrorResponse.NotFound(message = "No media found with id $id")

        val now = Clock.System.now()
        entity.deletedAt = now.toEpochMilliseconds()
        entity.updatedAt = now.toEpochMilliseconds()

        RepositoryResult.Success(true)
    }

}