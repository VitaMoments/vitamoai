package eu.vitamo.app.features.media.usecase

import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.features.media.model.MediaContent
import eu.vitamo.app.features.media.policy.MediaAccessPolicy
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.storage.MediaStorage
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class GetMediaContentUseCase(
    private val mediaRepository: MediaAssetRepository,
    private val mediaAccessPolicy: MediaAccessPolicy,
    private val mediaStorage: MediaStorage,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        mediaId: Uuid,
    ): RepositoryResult<MediaContent> {
        val asset = when (
            val result = mediaRepository.findById(mediaId)
        ) {
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                return result
        }

        if (
            asset.status != MediaStatus.READY ||
            asset.deletedAt != null
        ) {
            return RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "Media asset was not found.",
                ),
            )
        }

        val mayRead = mediaAccessPolicy.canRead(
            currentUserId = currentUserId,
            asset = asset,
        )

        if (!mayRead) {
            return RepositoryResult.Error(
                error = RepositoryError.Forbidden(
                    message = "You do not have access to this media asset.",
                ),
            )
        }

        val readHandle = mediaStorage.open(
            storageKey = asset.storageKey,
        ) ?: return RepositoryResult.Error(
            error = RepositoryError.Internal(
                message = "Media storage object is missing.",
            ),
        )

        return RepositoryResult.Success(
            data = MediaContent(
                asset = asset,
                readHandle = readHandle,
            ),
        )
    }
}