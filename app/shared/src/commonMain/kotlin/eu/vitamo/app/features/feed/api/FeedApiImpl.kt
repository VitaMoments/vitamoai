package eu.vitamo.app.features.feed.api

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.request.CreateFeedItemRequest
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.image.ImageCompressor
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class FeedApiImpl(
    private val client: HttpClient,
    private val authSessionCoordinator: AuthSessionCoordinator,
    private val imageCompressor: ImageCompressor,
    private val json: Json
) : FeedApi {

    override suspend fun getFeed(
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<FeedItem>> =
        safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString = "feed",
            ) {
                parameter(
                    key = "limit",
                    value = limit,
                )

                parameter(
                    key = "offset",
                    value = offset,
                )
            }
        }

    override suspend fun createFeedItem(
        request: CreateFeedItemRequest,
        images: List<PickedImage>,
    ): ApiResult<FeedItem> {
        val payload = json.encodeToString<CreateFeedItemRequest>(
            request,
        )

        val imageParts = try {
            images.map { image ->

                val compressed =
                    imageCompressor.compress(
                        image = image,
                        maxDimension = MAX_IMAGE_DIMENSION,
                        quality = JPEG_QUALITY,
                        maxBytes = MAX_COMPRESSED_IMAGE_BYTES,
                    )

                ImagePart(
                    fileName = compressed.fileName,
                    mimeType = compressed.mimeType,
                    bytes = compressed.bytes,
                )
            }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Exception) {
            throw cause
        }

        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.post(
                urlString = "feed",
            ) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "payload",
                                value = payload,
                            )

                            imageParts.forEach { image ->
                                append(
                                    key = "file",
                                    value = image.bytes,
                                    headers = Headers.build {
                                        append(
                                            name = HttpHeaders.ContentType,
                                            value = image.mimeType,
                                        )

                                        append(
                                            name = HttpHeaders.ContentDisposition,
                                            value =
                                                "filename=\"${image.safeFileName()}\"",
                                        )
                                    },
                                )
                            }
                        },
                    ),
                )
            }
        }
    }

    private fun ImagePart.safeFileName(): String =
        fileName
            .replace("\"", "")
            .take(MAX_FILE_NAME_LENGTH)

    private data class ImagePart(
        val fileName: String,
        val mimeType: String,
        val bytes: ByteArray,
    )

    private companion object {

        const val MAX_IMAGE_BYTES =
            10L * 1024L * 1024L

        const val MAX_FILE_NAME_LENGTH =
            128

        const val MAX_IMAGE_DIMENSION = 1920
        const val JPEG_QUALITY = 82

        const val MAX_COMPRESSED_IMAGE_BYTES =
            3L * 1024L * 1024L
    }
}