package eu.vitamo.app.features.media.routes

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.features.media.model.MediaContent
import eu.vitamo.app.features.media.storage.MediaReadHandle
import eu.vitamo.app.features.media.usecase.GetMediaContentUseCase
import eu.vitamo.app.features.media.usecase.UploadImageUseCase
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import org.jetbrains.exposed.v1.core.exposedLogger
import org.koin.ktor.ext.inject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.Uuid

fun Route.mediaRoutes() {
    val uploadImageUseCase: UploadImageUseCase by inject()
    val getMediaContentUseCase: GetMediaContentUseCase by inject()

    route("/media") {
        post("/images") {
            val currentUserId = call.requireUserId()
            val upload = call.receiveImageUpload()

            call.handleResult(
                result = uploadImageUseCase(
                    ownerId = currentUserId,
                    source = upload.temporaryFile,
                    purpose = upload.purpose,
                    visibility = upload.visibility,
                ),
                successStatusCode = HttpStatusCode.Created,
            )
        }

        get("/{mediaId}/content") {
            val currentUserId = call.requireUserId()
            val mediaId = call.parameters["mediaId"]
                ?.let { runCatching { Uuid.parse(it) } }
                ?.getOrNull() ?: throw ApiException.BadRequest("parameter mediaId not valid")

            exposedLogger.warn("AccessMedia: userId{$currentUserId} content{$mediaId}")

            call.handleResult(
                result = getMediaContentUseCase(
                    currentUserId = currentUserId,
                    mediaId = mediaId,
                ),
                onSuccess = { mediaContent ->
                    respondMediaContent(mediaContent)
                },
            )
        }
    }
}

private data class ReceivedImageUpload(
    val temporaryFile: Path,
    val purpose: MediaPurpose,
    val visibility: MediaVisibility,
)

private suspend fun ApplicationCall.receiveImageUpload(): ReceivedImageUpload {
    var temporaryFile: Path? = null
    var purpose: MediaPurpose? = null
    var visibility: MediaVisibility? = null

    try {
        val multipart = receiveMultipart(
            formFieldLimit = MAX_MULTIPART_BYTES,
        )

        multipart.forEachPart { part ->
            try {
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "purpose" -> {
                                purpose = part.value
                                    .toEnumOrNull<MediaPurpose>()
                                    ?: throw ApiException.BadRequest(
                                        message = "Invalid media purpose.",
                                    )
                            }

                            "visibility" -> {
                                visibility = part.value
                                    .toEnumOrNull<MediaVisibility>()
                                    ?: throw ApiException.BadRequest(
                                        message = "Invalid media visibility.",
                                    )
                            }
                        }
                    }

                    is PartData.FileItem -> {
                        if (part.name != "file") {
                            return@forEachPart
                        }

                        if (temporaryFile != null) {
                            throw ApiException.BadRequest(
                                message = "Only one image can be uploaded.",
                            )
                        }

                        val target = Files.createTempFile(
                            "vitamo-image-upload-",
                            ".tmp",
                        )

                        temporaryFile = target

                        part.provider().copyAndClose(
                            target.toFile().writeChannel(),
                        )
                    }

                    else -> Unit
                }
            } finally {
                part.release()
            }
        }

        val file = temporaryFile
            ?: throw ApiException.BadRequest(
                message = "Image file was not provided.",
            )

        return ReceivedImageUpload(
            temporaryFile = file,
            purpose = purpose
                ?: throw ApiException.BadRequest(
                    message = "Media purpose was not provided.",
                ),
            visibility = visibility
                ?: throw ApiException.BadRequest(
                    message = "Media visibility was not provided.",
                ),
        )
    } catch (cause: IOException) {
        temporaryFile?.let {
            Files.deleteIfExists(it)
        }

        throw ApiException.BadRequest(
            message = "The uploaded image is too large or invalid.",
        )
    } catch (cause: Throwable) {
        temporaryFile?.let {
            Files.deleteIfExists(it)
        }

        throw cause
    }
}

private suspend fun ApplicationCall.respondMediaContent(
    content: MediaContent,
) {
    response.header(
        name = HttpHeaders.ETag,
        value = "\"${content.asset.sha256}\"",
    )

    response.header(
        name = HttpHeaders.ContentDisposition,
        value = ContentDisposition.Inline.toString(),
    )

    response.header(
        name = HttpHeaders.CacheControl,
        value = when (content.asset.visibility) {
            MediaVisibility.PUBLIC ->
                "public, max-age=31536000, immutable"

            MediaVisibility.AUTHENTICATED,
            MediaVisibility.FRIENDS,
            MediaVisibility.PRIVATE,
                ->
                "private, no-store"
        },
    )

    when (val handle = content.readHandle) {
        is MediaReadHandle.LocalFile -> {
            respondFile(
                file = handle.path.toFile(),
            )
        }

        is MediaReadHandle.Redirect -> {
            respondRedirect(
                url = handle.url,
                permanent = false,
            )
        }
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return enumValues<T>()
        .firstOrNull { value ->
            value.name.equals(
                other = trim(),
                ignoreCase = true,
            )
        }
}

private const val MAX_MULTIPART_BYTES =
    10L * 1024L * 1024L + 64L * 1024L