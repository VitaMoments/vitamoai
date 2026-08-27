package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.features.feed.model.FeedMediaUpload
import eu.vitamo.app.features.feed.usecase.GetFeedUseCase
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.infrastructure.network.helpers.getPaginationParameters
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.feedRoutes() {
    val json: Json by inject()
    val getFeedUseCase: GetFeedUseCase by inject()
    val createFeedItemUseCase: CreateFeedItemUseCase by inject()

    route("/feed") {
//        Feed(Item) routes
        get {
            val currentUserId = call.requireUserId()
            val (limit, offset) = call.getPaginationParameters()

            call.handleResult(
                getFeedUseCase(currentUserId, limit, offset.toLong())
            )
        }
        post {
            val currentUserId = call.requireUserId()
            var payload: CreateFeedItemRequest? = null

            val files = mutableListOf<FeedMediaUpload>()
            val uploadLogEntries = mutableListOf<MultipartUploadLogEntry>()

            call.receiveMultipart(
                formFieldLimit = 1024*1024*100L
            ).forEachPart { part ->
                when(part) {
                    is PartData.FormItem -> {
                        if (part.name == "payload") {
                            payload = part.getPayload<CreateFeedItemRequest>(json)
                        }
                        part.release()
                    }
                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            val contentType = part.contentType?.withoutParameters()?.toString()
                            val bytes = part.readAllBytesAndDispose()
                            files += FeedMediaUpload(
                                bytes = bytes,
                                fileName = part.originalFileName,
                                mimeType = contentType
                                    ?.takeUnless { it.equals("application/octet-stream", ignoreCase = true) }
                                    ?: guessContentTypeFromFileName(part.originalFileName),
                            )
                            uploadLogEntries += MultipartUploadLogEntry(
                                name = part.originalFileName,
                                type = contentType,
                                size = bytes.size.toLong(),
                            )
                        } else {
                            part.release()
                        }
                    }
                    else -> part.release()
                }
            }
            call.logMultipartUpload(uploadLogEntries)

            val result = createFeedItemUseCase.invoke(
                userId = currentUserId,
                request = payload,
                files = files
            )
            call.handleResult(result = result, HttpStatusCode.Created)
        }
        delete {

        }

//        Comment & Likes
        put("/like") {

        }
        post("/reaction") {

        }
        delete("/reaction"){

        }
    }
}

private inline fun <reified T> PartData.FormItem.getPayload(
    json: Json
) : T? = runCatching { json.decodeFromString<T>(this.value) }.getOrNull()

suspend fun PartData.FileItem.readAllBytesAndDispose(): ByteArray {
    return try {
        val channel = provider()
        channel.readRemaining().readByteArray()
    } finally {
        release()
    }
}

fun guessContentTypeFromFileName(fileName: String?): String? {
    val ext = fileName
        ?.substringAfterLast('.', "")
        ?.lowercase()
        .orEmpty()

    return when (ext) {
        "jpg", "jpeg", "jpe", "jfif" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        else -> null
    }
}