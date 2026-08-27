package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.Post
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.feed.context.FeedItemContextLoader
import eu.vitamo.app.features.feed.model.FeedMediaUpload
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.usecase.UploadImageUseCase
import eu.vitamo.app.repository.FieldError
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CreatePostUseCase(
    private val feedItemRepository: FeedItemRepository,
    private val mediaAssetRepository: MediaAssetRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteMediaAssetUseCase: DeleteMediaAssetUseCase,
    private val feedItemContextLoader: FeedItemContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        title: String?,
        message: RichTextDocument?,
        files: List<FeedMediaUpload>,
    ): RepositoryResult<Post> {
        if (files.isEmpty()) {
            return RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "files",
                            message = "At least one image is required.",
                        ),
                    ),
                    message = "Media is required.",
                ),
            )
        }

        val normalizedTitle = title
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        val messageJson = message
            ?.let { document ->
                Json.encodeToString<RichTextDocument>(
                    document,
                )
            }

        /*
         * Eerst alle bestanden uploaden.
         *
         * UploadImageUseCase doet:
         *
         * - image processing
         * - createPending
         * - storage
         * - markReady
         *
         * Daarna hebben we READY MediaAssets die we
         * transactioneel aan de nieuwe post kunnen koppelen.
         */
        val mediaIds = when (
            val result = uploadMedia(
                currentUserId = currentUserId,
                files = files,
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
         * Alleen database-operaties zitten in deze transaction:
         *
         * - feed_item
         * - post
         * - media -> feed_item koppeling
         *
         * De fysieke bestanden zijn hierboven al opgeslagen.
         */
        val postRecord = try {
            dbQuery {
                val createdPost = when (
                    val result = feedItemRepository.createPost(
                        authorId = currentUserId,
                        title = normalizedTitle,
                        messageJson = messageJson,
                        createdAt = Clock.System.now(),
                    )
                ) {
                    is RepositoryResult.Success -> {
                        result.data
                    }

                    is RepositoryResult.Error -> {
                        throw TransactionAbortedException(
                            error = result.error,
                        )
                    }
                }

                when (
                    val result =
                        mediaAssetRepository.attachAllToFeedItem(
                            mediaIds = mediaIds,
                            feedItemId = createdPost.id,
                        )
                ) {
                    is RepositoryResult.Success -> Unit

                    is RepositoryResult.Error -> {
                        throw TransactionAbortedException(
                            error = result.error,
                        )
                    }
                }

                createdPost
            }
        } catch (cause: TransactionAbortedException) {
            cleanupMedia(
                mediaIds = mediaIds,
            )

            return RepositoryResult.Error(
                error = cause.error,
            )
        } catch (cause: CancellationException) {
            cleanupMedia(
                mediaIds = mediaIds,
            )

            throw cause
        } catch (cause: Throwable) {
            cleanupMedia(
                mediaIds = mediaIds,
            )

            return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message = "The post could not be created.",
                    cause = cause,
                ),
            )
        }

        /*
         * Vanaf hier is de database-transaction gecommit.
         *
         * Als context loading faalt verwijderen we de post
         * daarom NIET meer. De post en media zijn geldig
         * opgeslagen en kunnen later opnieuw worden opgehaald.
         */
        val post = try {
            feedItemContextLoader.load(
                currentUserId = currentUserId,
                feedItem = postRecord,
            )
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message =
                        "The created post could not be loaded.",
                    cause = cause,
                ),
            )
        }
            ?: return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message =
                        "The created post could not be loaded.",
                ),
            )

        return RepositoryResult.Success(
            data = post,
        )
    }

    private suspend fun uploadMedia(
        currentUserId: Uuid,
        files: List<FeedMediaUpload>,
    ): RepositoryResult<List<Uuid>> {
        val uploadedMediaIds =
            mutableListOf<Uuid>()

        try {
            files.forEachIndexed { index, file ->
                val source = createTempFile(
                    upload = file,
                    index = index,
                )

                when (
                    val result = uploadImageUseCase(
                        ownerId = currentUserId,
                        source = source,
                        purpose =
                            MediaPurpose.FEED_ATTACHMENT,
                        visibility =
                            MediaVisibility.AUTHENTICATED,
                    )
                ) {
                    is RepositoryResult.Success -> {
                        uploadedMediaIds +=
                            result.data.id
                    }

                    is RepositoryResult.Error -> {
                        cleanupMedia(
                            mediaIds =
                                uploadedMediaIds,
                        )

                        return RepositoryResult.Error(
                            error = result.error,
                        )
                    }
                }
            }
        } catch (cause: CancellationException) {
            cleanupMedia(
                mediaIds = uploadedMediaIds,
            )

            throw cause
        } catch (cause: Throwable) {
            cleanupMedia(
                mediaIds = uploadedMediaIds,
            )

            return RepositoryResult.Error(
                error = RepositoryError.Internal(
                    message =
                        "The media could not be uploaded.",
                    cause = cause,
                ),
            )
        }

        return RepositoryResult.Success(
            data = uploadedMediaIds,
        )
    }

    private fun createTempFile(
        upload: FeedMediaUpload,
        index: Int,
    ): Path {
        val suffix =
            determineFileSuffix(
                upload = upload,
            )

        val path = Files.createTempFile(
            "vitamo-feed-$index-",
            suffix,
        )

        try {
            Files.write(
                path,
                upload.bytes,
            )

            return path
        } catch (cause: Throwable) {
            runCatching {
                Files.deleteIfExists(
                    path,
                )
            }

            throw cause
        }
    }

    private fun determineFileSuffix(
        upload: FeedMediaUpload,
    ): String {
        val fileExtension = upload.fileName
            ?.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = "",
            )
            ?.trim()
            ?.lowercase()
            ?.takeIf(String::isNotEmpty)
            ?.filter(Char::isLetterOrDigit)
            ?.take(MAX_EXTENSION_LENGTH)
            ?.takeIf(String::isNotEmpty)

        if (fileExtension != null) {
            return ".$fileExtension"
        }

        return when (
            upload.mimeType
                ?.substringBefore(';')
                ?.trim()
                ?.lowercase()
        ) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/heic" -> ".heic"
            "image/heif" -> ".heif"

            else -> ".img"
        }
    }

    private suspend fun cleanupMedia(
        mediaIds: Collection<Uuid>,
    ) {
        if (mediaIds.isEmpty()) {
            return
        }

        /*
         * Ook bij coroutine cancellation moeten reeds
         * opgeslagen bestanden worden opgeruimd.
         */
        withContext(NonCancellable) {
            mediaIds
                .toList()
                .asReversed()
                .forEach { mediaId ->
                    runCatching {
                        deleteMediaAssetUseCase(
                            mediaId = mediaId,
                        )
                    }
                }
        }
    }

    private class TransactionAbortedException(
        val error: RepositoryError,
    ) : RuntimeException(
        error.message,
    )

    private companion object {
        const val MAX_EXTENSION_LENGTH = 10
    }
}