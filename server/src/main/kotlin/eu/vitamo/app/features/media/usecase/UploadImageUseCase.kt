package eu.vitamo.app.features.media.usecase

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.features.media.mapper.toContract
import eu.vitamo.app.features.media.model.CreateMediaAssetInput
import eu.vitamo.app.features.media.processor.ImageProcessor
import eu.vitamo.app.features.media.processor.InvalidImageException
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.storage.MediaStorage
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.Uuid

class UploadImageUseCase(
    private val mediaRepository: MediaAssetRepository,
    private val imageProcessor: ImageProcessor,
    private val mediaStorage: MediaStorage,
) {

    suspend operator fun invoke(
        ownerId: Uuid,
        source: Path,
        purpose: MediaPurpose,
        visibility: MediaVisibility,
    ): RepositoryResult<MediaAsset> {
        var processedPath: Path? = null
        var mediaId: Uuid? = null
        var storageKey: String? = null

        try {
            val processedImage = imageProcessor.process(source)

            processedPath = processedImage.path

            val pendingRecord = when (
                val result = mediaRepository.createPending(
                    input = CreateMediaAssetInput(
                        ownerId = ownerId,
                        purpose = purpose,
                        visibility = visibility,
                        mimeType = processedImage.mimeType,
                        extension = processedImage.extension,
                        sizeBytes = processedImage.sizeBytes,
                        width = processedImage.width,
                        height = processedImage.height,
                        sha256 = processedImage.sha256,
                    ),
                )
            ) {
                is RepositoryResult.Success ->
                    result.data

                is RepositoryResult.Error ->
                    return result
            }

            mediaId = pendingRecord.id
            storageKey = pendingRecord.storageKey

            mediaStorage.store(
                storageKey = pendingRecord.storageKey,
                source = processedImage.path,
            )

            return when (
                val result = mediaRepository.markReady(
                    id = pendingRecord.id,
                )
            ) {
                is RepositoryResult.Success -> {
                    RepositoryResult.Success(
                        data = result.data.toContract(),
                    )
                }

                is RepositoryResult.Error -> {
                    mediaStorage.delete(
                        storageKey = pendingRecord.storageKey,
                    )

                    mediaRepository.markFailed(
                        id = pendingRecord.id,
                    )

                    result
                }
            }
        } catch (cause: InvalidImageException) {
            return RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    message = cause.message,
                ),
            )
        } catch (cause: Throwable) {
            if (storageKey != null) {
                runCatching {
                    mediaStorage.delete(storageKey)
                }
            }

            if (mediaId != null) {
                runCatching {
                    mediaRepository.markFailed(mediaId)
                }
            }

            return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message = "The image could not be stored.",
                ),
            )
        } finally {
            runCatching {
                Files.deleteIfExists(source)
            }

            if (processedPath != null) {
                runCatching {
                    Files.deleteIfExists(processedPath)
                }
            }
        }
    }
}