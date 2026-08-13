package eu.vitamo.app.features.media.api

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class MediaApiImpl(
    private val client: HttpClient,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : MediaApi {

    override suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        purpose: MediaPurpose,
        visibility: MediaVisibility,
    ): ApiResult<MediaAsset> {
        require(bytes.isNotEmpty()) {
            "Image cannot be empty."
        }

        require(bytes.size <= MAX_IMAGE_BYTES) {
            "Image exceeds the client upload limit."
        }

        val safeFileName = fileName
            .replace("\"", "")
            .take(128)

        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.post(
                urlString = "media/images",
            ) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "purpose",
                                value = purpose.name,
                            )

                            append(
                                key = "visibility",
                                value = visibility.name,
                            )

                            append(
                                key = "file",
                                value = bytes,
                                headers = Headers.build {
                                    append(
                                        name = HttpHeaders.ContentType,
                                        value = mimeType,
                                    )

                                    append(
                                        name = HttpHeaders.ContentDisposition,
                                        value = "filename=\"$safeFileName\"",
                                    )
                                },
                            )
                        },
                    ),
                )
            }
        }
    }

    private companion object {
        const val MAX_IMAGE_BYTES =
            10 * 1024 * 1024
    }
}