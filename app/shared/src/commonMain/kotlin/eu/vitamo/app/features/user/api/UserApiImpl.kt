package eu.vitamo.app.features.user.api

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class UserApiImpl(
    private val client: HttpClient,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : UserApi {

    override suspend fun getUser(
        userId: Uuid,
    ): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString = "users/$userId",
            )
        }
    }

    override suspend fun getCurrentUser(): ApiResult<UserWithContext> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator
        ) {
            client.get(
                urlString = "users/me"
            )
        }
    }

    override suspend fun searchUsers(
        query: String?,
        limit: Int,
        offset: Long,
    ): ApiResult<PagedResult<UserWithContext>> {
        return safeAuthenticatedApiCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.get(
                urlString = "users/search",
            ) {
                query
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let { value ->
                        parameter(
                            key = "query",
                            value = value,
                        )
                    }

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
    }

    override suspend fun updateProfileImage(
        image: PickedImage,
    ): ApiResult<UserWithContext> {
        return try {
            val bytes = image.readBytes(
                maxBytes = MAX_PROFILE_IMAGE_BYTES,
            )

            val safeFileName = image.fileName
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "")
                .take(MAX_FILE_NAME_LENGTH)
                .ifBlank {
                    "profile-image.jpg"
                }

            safeAuthenticatedApiCall(
                authSessionCoordinator = authSessionCoordinator,
            ) {
                client.put(
                    urlString = "users/me/profile-image",
                ) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    key = "file",
                                    value = bytes,
                                    headers = Headers.build {
                                        append(
                                            name = HttpHeaders.ContentType,
                                            value = image.mimeType,
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
        } finally {
            withContext(NonCancellable) {
                runCatching {
                    image.cleanup()
                }
            }
        }
    }

    private companion object {
        const val MAX_PROFILE_IMAGE_BYTES =
            10L * 1024L * 1024L

        const val MAX_FILE_NAME_LENGTH =
            128
    }
}