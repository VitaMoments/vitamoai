package eu.vitamo.app.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.media.MediaAssetContext
import eu.vitamo.app.api.contracts.media.MediaCreateInput
import eu.vitamo.app.api.contracts.media.MediaPurposeType
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import eu.vitamo.app.error.ErrorResponse
import eu.vitamo.app.features.media.repository.MediaRepository
import eu.vitamo.app.media.model.StoredMedia
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class MediaService(
    private val mediaRepository: MediaRepository,
    private val mediaStorage: MediaStorage,
    private val mediaValidationService: MediaValidationService,
    private val mediaAccessService: MediaAccessService
) {

    suspend fun uploadMedia(
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType,
        privacy: PrivacyStatus,
        originalFileName: String?,
        contentType: String?,
        bytes: ByteArray,
        createdBy: Uuid?
    ): RepositoryResult<MediaAssetContext> {
        val canUpload = mediaAccessService.canUpload(
            referenceId = referenceId,
            referenceType = referenceType,
            requesterUserId = createdBy
        )

        if (!canUpload) {
            throw ErrorResponse.Forbidden("You are not allowed to upload media for this reference")
        }

        val validation = mediaValidationService.validateImage(
            contentType = contentType,
            bytes = bytes
        )

        if (validation.isFailure) {
            throw ErrorResponse.BadRequest(validation.exceptionOrNull()?.message ?: "Invalid file")
        }

        val safeContentType = requireNotNull(mediaValidationService.canonicalContentType(contentType))
        val extension = mediaValidationService.extensionFor(safeContentType)
        val mediaId = Uuid.random()

        val objectKey = mediaStorage.buildObjectKey(
            referenceType = referenceType.name,
            referenceId = referenceId.toString(),
            mediaId = mediaId.toString(),
            fileExtension = extension
        )

        return try {
            mediaStorage.save(
                objectKey = objectKey,
                bytes = bytes,
                contentType = safeContentType
            )

            val input = MediaCreateInput(
                uuid = mediaId,
                referenceId = referenceId,
                referenceType = referenceType,
                purpose = purpose,
                privacy = privacy,
                originalFileName = originalFileName,
                storedFileName = "original.$extension",
                objectKey = objectKey,
                contentType = safeContentType,
                sizeBytes = bytes.size.toLong(),
                width = null,
                height = null,
                createdBy = createdBy
            )
            mediaRepository.create(input)
        } catch (t: Throwable) {
            try {
                mediaStorage.delete(objectKey)
            } catch (_: Throwable) { }
            throw ErrorResponse.Internal("Failed to store media: ${t.message}", t)
        }
    }

    suspend fun getMediaById(
        mediaId: Uuid
    ): RepositoryResult<MediaAssetContext> {
        return mediaRepository.findById(mediaId)
    }

    suspend fun readMedia(
        mediaId: Uuid,
        requesterUserId: Uuid?
    ): RepositoryResult<Pair<MediaAssetContext, StoredMedia>> {
        return when (val mediaResult = mediaRepository.findById(mediaId)) {
            is RepositoryResult.Error -> mediaResult
            is RepositoryResult.Success -> {
                val media = mediaResult.data

                val allowed = mediaAccessService.canView(
                    media = media,
                    requesterUserId = requesterUserId
                )

                if (!allowed) {
                    throw ErrorResponse.Forbidden("You are not allowed to view this media")
                } else {
                    try {
                        val stored = mediaStorage.read(media.objectKey)
                        RepositoryResult.Success(media to stored)
                    } catch (t: Throwable) {
                        throw ErrorResponse.Internal("Stored media could not be read: ${t.message}", t)
                    }
                }
            }
        }
    }

    suspend fun deleteMedia(
        mediaId: Uuid,
        requesterUserId: Uuid?
    ): RepositoryResult<Boolean> {
        val media = when (val mediaResult = mediaRepository.findById(mediaId)) {
            is RepositoryResult.Error -> return mediaResult
            is RepositoryResult.Success -> mediaResult.data
        }

        val allowed = mediaAccessService.canDelete(
            media = media,
            requesterUserId = requesterUserId
        )

        if (!allowed) {
            throw ErrorResponse.Forbidden("You are not allowed to delete this media")
        }

        return when (val deleteResult = mediaRepository.softDelete(mediaId, requesterUserId!!)) {
            is RepositoryResult.Error -> deleteResult
            is RepositoryResult.Success -> {
                try {
                    mediaStorage.delete(media.objectKey)
                } catch (_: Throwable) {
                }

                RepositoryResult.Success(true)
            }
        }
    }

    suspend fun findAllByReference(
        referenceId: Uuid,
        referenceType: MediaReferenceType
    ): RepositoryResult<List<MediaAssetContext>> {
        return mediaRepository.findAllByReference(
            referenceId = referenceId,
            referenceType = referenceType
        )
    }

    suspend fun findAllByReferenceAndPurpose(
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType
    ): RepositoryResult<List<MediaAssetContext>> {
        return mediaRepository.findAllByReferenceAndPurpose(
            referenceId = referenceId,
            referenceType = referenceType,
            purpose = purpose
        )
    }
}