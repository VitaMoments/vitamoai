package eu.vitamo.app.features.media.repository

import eu.vitamo.app.features.media.model.CreateMediaAssetInput
import eu.vitamo.app.features.media.model.MediaAssetRecord
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface MediaAssetRepository {

    suspend fun createPending(
        input: CreateMediaAssetInput,
    ): RepositoryResult<MediaAssetRecord>

    suspend fun findById(
        id: Uuid,
    ): RepositoryResult<MediaAssetRecord>

    suspend fun markReady(
        id: Uuid,
    ): RepositoryResult<MediaAssetRecord>

    suspend fun markFailed(
        id: Uuid,
    ): RepositoryResult<Unit>

    suspend fun markDeleted(
        id: Uuid,
    ): RepositoryResult<Unit>
}