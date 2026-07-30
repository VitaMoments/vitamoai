package eu.vitamo.app.features.media.usecase

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.media.model.CreateMediaAssetInput
import eu.vitamo.app.features.media.model.MediaAssetRecord
import eu.vitamo.app.features.media.processor.ImageProcessor
import eu.vitamo.app.features.media.processor.InvalidImageException
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.storage.MediaStorage
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class UpdateProfileImageUseCase(
    private val userRepository: UserRepository,
    private val mediaAssetRepository: MediaAssetRepository,
    private val mediaStorage: MediaStorage,
    private val imageProcessor: ImageProcessor,
    private val userContextLoader: UserContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        source: Path,
    ): RepositoryResult<UserWithContext> {
        var processedPath: Path? = null
        var newAsset: MediaAssetRecord? = null
        var storageObjectCreated = false
        var profileImageLinked = false

        try {
            val processedImage = try {
                imageProcessor.process(
                    source = source,
                )
            } catch (cause: InvalidImageException) {
                return RepositoryResult.Error(
                    error = RepositoryError.BadRequest(
                        message = cause.message,
                    ),
                )
            }

            processedPath = processedImage.path

            val pendingAsset = when (
                val result = mediaAssetRepository.createPending(
                    input = CreateMediaAssetInput(
                        ownerId = currentUserId,
                        purpose = MediaPurpose.PROFILE_IMAGE,
                        visibility =
                            MediaVisibility.AUTHENTICATED,
                        mimeType = processedImage.mimeType,
                        extension = processedImage.extension,
                        sizeBytes = processedImage.sizeBytes,
                        width = processedImage.width,
                        height = processedImage.height,
                        sha256 = processedImage.sha256,
                    ),
                )
            ) {
                is RepositoryResult.Success -> {
                    result.data
                }

                is RepositoryResult.Error -> {
                    return result
                }
            }

            newAsset = pendingAsset

            mediaStorage.store(
                storageKey = pendingAsset.storageKey,
                source = processedImage.path,
            )

            storageObjectCreated = true

            when (
                val readyResult =
                    mediaAssetRepository.markReady(
                        id = pendingAsset.id,
                    )
            ) {
                is RepositoryResult.Success -> Unit

                is RepositoryResult.Error -> {
                    compensateFailedUpload(
                        asset = pendingAsset,
                        storageObjectCreated =
                            storageObjectCreated,
                    )

                    return readyResult
                }
            }

            val profileImageUpdate = when (
                val result = userRepository.replaceProfileImage(
                    userId = currentUserId,
                    profileImageId = pendingAsset.id,
                )
            ) {
                is RepositoryResult.Success -> {
                    result.data
                }

                is RepositoryResult.Error -> {
                    compensateFailedUpload(
                        asset = pendingAsset,
                        storageObjectCreated =
                            storageObjectCreated,
                    )

                    return result
                }
            }

            profileImageLinked = true

            val userWithContext = userContextLoader.load(
                currentUserId = currentUserId,
                targetUser = profileImageUpdate.user,
            )

            cleanupPreviousProfileImage(
                currentUserId = currentUserId,
                previousProfileImageId =
                    profileImageUpdate.previousProfileImageId,
                newProfileImageId = pendingAsset.id,
            )

            return RepositoryResult.Success(
                data = userWithContext,
            )
        } catch (cause: CancellationException) {
            if (!profileImageLinked) {
                newAsset?.let { asset ->
                    compensateFailedUpload(
                        asset = asset,
                        storageObjectCreated =
                            storageObjectCreated,
                    )
                }
            }

            throw cause
        } catch (_: Throwable) {
            if (!profileImageLinked) {
                newAsset?.let { asset ->
                    compensateFailedUpload(
                        asset = asset,
                        storageObjectCreated =
                            storageObjectCreated,
                    )
                }
            }

            return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message =
                        "The profile image could not be updated.",
                ),
            )
        } finally {
            cleanupTemporaryFiles(
                source = source,
                processedPath = processedPath,
            )
        }
    }

    private suspend fun compensateFailedUpload(
        asset: MediaAssetRecord,
        storageObjectCreated: Boolean,
    ) {
        if (storageObjectCreated) {
            runCatching {
                mediaStorage.delete(
                    storageKey = asset.storageKey,
                )
            }
        }

        runCatching {
            mediaAssetRepository.markFailed(
                id = asset.id,
            )
        }
    }

    private suspend fun cleanupPreviousProfileImage(
        currentUserId: Uuid,
        previousProfileImageId: Uuid?,
        newProfileImageId: Uuid,
    ) {
        if (
            previousProfileImageId == null ||
            previousProfileImageId == newProfileImageId
        ) {
            return
        }

        val previousAsset = when (
            val result = mediaAssetRepository.findById(
                id = previousProfileImageId,
            )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                return
            }
        }

        /*
         * Verwijder uitsluitend een media-asset die echt van deze
         * gebruiker is en als profielfoto is aangemaakt.
         */
        if (
            previousAsset.ownerId != currentUserId ||
            previousAsset.purpose !=
            MediaPurpose.PROFILE_IMAGE
        ) {
            return
        }

        val markedDeleted = when (
            mediaAssetRepository.markDeleted(
                id = previousAsset.id,
            )
        ) {
            is RepositoryResult.Success -> true
            is RepositoryResult.Error -> false
        }

        if (!markedDeleted) {
            return
        }

        /*
         * Het databaseobject is nu niet meer zichtbaar.
         * Het verwijderen uit storage is best effort:
         * een mislukte cleanup mag de nieuwe profielfoto niet terugdraaien.
         */
        runCatching {
            mediaStorage.delete(
                storageKey = previousAsset.storageKey,
            )
        }
    }

    private suspend fun cleanupTemporaryFiles(
        source: Path,
        processedPath: Path?,
    ) {
        withContext(
            NonCancellable + Dispatchers.IO,
        ) {
            runCatching {
                Files.deleteIfExists(source)
            }

            if (
                processedPath != null &&
                processedPath != source
            ) {
                runCatching {
                    Files.deleteIfExists(processedPath)
                }
            }
        }
    }
}