package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaAssetContext
import eu.vitamo.app.api.contracts.media.MediaCreateInput
import eu.vitamo.app.api.contracts.media.MediaPurposeType
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface MediaRepository {
    suspend fun create(input: MediaCreateInput) : RepositoryResult<MediaAssetContext>
    suspend fun findById(id: Uuid) : RepositoryResult<MediaAssetContext>
    suspend fun findAllByReference(
        referenceId: Uuid,
        referenceType: MediaReferenceType
    ): RepositoryResult<List<MediaAssetContext>>
    suspend fun findProfileImage(
        userId: Uuid
    ): RepositoryResult<MediaAssetContext?>
    suspend fun findAllByReferenceAndPurpose(
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType
    ): RepositoryResult<List<MediaAssetContext>>
    suspend fun softDelete(id: Uuid, deletedBy: Uuid): RepositoryResult<Boolean>
}