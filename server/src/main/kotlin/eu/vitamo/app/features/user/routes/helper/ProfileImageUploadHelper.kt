package eu.vitamo.app.features.user.routes.helper

import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProfileImageUpload(
    val temporaryFile: Path,
)

suspend fun ApplicationCall.receiveProfileImageUpload():
        RepositoryResult<ProfileImageUpload> {
    var temporaryFile: Path? = null
    var imageReceived = false

    return try {
        val multipart = receiveMultipart(
            formFieldLimit = MAX_MULTIPART_BYTES,
        )

        multipart.forEachPart { part ->
            try {
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name != FILE_FIELD_NAME) {
                            return@forEachPart
                        }

                        if (imageReceived) {
                            throw ProfileImageUploadException(
                                message = "Er mag maar één afbeelding worden geüpload.",
                            )
                        }

                        val target = withContext(Dispatchers.IO) {
                            Files.createTempFile(
                                TEMP_FILE_PREFIX,
                                TEMP_FILE_SUFFIX,
                            )
                        }

                        temporaryFile = target

                        part.provider().copyAndClose(
                            target.toFile().writeChannel(),
                        )

                        val sizeBytes = withContext(Dispatchers.IO) {
                            Files.size(target)
                        }

                        if (sizeBytes <= 0L) {
                            throw ProfileImageUploadException(
                                message = "De geüploade afbeelding is leeg.",
                            )
                        }

                        if (sizeBytes > MAX_PROFILE_IMAGE_BYTES) {
                            throw ProfileImageUploadException(
                                message = "De afbeelding mag maximaal 10 MB groot zijn.",
                            )
                        }

                        imageReceived = true
                    }

                    else -> Unit
                }
            } finally {
                part.release()
            }
        }

        val file = temporaryFile

        if (!imageReceived || file == null) {
            cleanupTemporaryFile(temporaryFile)

            RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    message = "Er is geen afbeelding meegestuurd.",
                ),
            )
        } else {
            RepositoryResult.Success(
                data = ProfileImageUpload(
                    temporaryFile = file,
                ),
            )
        }
    } catch (cause: CancellationException) {
        cleanupTemporaryFile(temporaryFile)
        throw cause
    } catch (cause: ProfileImageUploadException) {
        cleanupTemporaryFile(temporaryFile)

        RepositoryResult.Error(
            error = RepositoryError.BadRequest(
                message = cause.message,
            ),
        )
    } catch (_: Throwable) {
        cleanupTemporaryFile(temporaryFile)

        RepositoryResult.Error(
            error = RepositoryError.BadRequest(
                message = "De afbeelding kon niet worden ontvangen.",
            ),
        )
    }
}

private suspend fun cleanupTemporaryFile(
    path: Path?,
) {
    if (path == null) {
        return
    }

    withContext(Dispatchers.IO) {
        runCatching {
            Files.deleteIfExists(path)
        }
    }
}

private class ProfileImageUploadException(
    override val message: String,
) : RuntimeException(message)

private const val FILE_FIELD_NAME =
    "file"

private const val TEMP_FILE_PREFIX =
    "vitamo-profile-image-"

private const val TEMP_FILE_SUFFIX =
    ".upload"

private const val MAX_PROFILE_IMAGE_BYTES =
    10L * 1024L * 1024L

private const val MULTIPART_OVERHEAD_BYTES =
    64L * 1024L

private const val MAX_MULTIPART_BYTES =
    MAX_PROFILE_IMAGE_BYTES + MULTIPART_OVERHEAD_BYTES