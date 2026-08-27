package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.storage.MediaStorage
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.CancellationException
import kotlin.uuid.Uuid

class DeleteMediaAssetUseCase(
    private val mediaRepository:
        MediaAssetRepository,
    private val mediaStorage:
        MediaStorage,
) {

    suspend operator fun invoke(
        mediaId: Uuid,
    ): RepositoryResult<Unit> {
        val media = when (
            val result =
                mediaRepository.findById(
                    id = mediaId,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                return result
            }
        }

        /*
         * Eerst database record logisch verwijderen.
         *
         * Als physical storage cleanup daarna faalt,
         * verschijnt het asset in ieder geval niet meer
         * als actief mediarecord.
         */
        when (
            val result =
                mediaRepository.markDeleted(
                    id = mediaId,
                )
        ) {
            is RepositoryResult.Success -> Unit

            is RepositoryResult.Error -> {
                return result
            }
        }

        return try {
            mediaStorage.delete(
                storageKey = media.storageKey,
            )

            RepositoryResult.Success(
                data = Unit,
            )
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message =
                        "The media file could not be deleted.",
                    cause = cause,
                ),
            )
        }
    }
}