package eu.vitamo.app.features.device.api

import eu.vitamo.app.api.contracts.device.UpdateFirebaseInstallationIdRequest
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedUnitCall
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class DeviceApiImpl(
    private val client: HttpClient,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : DeviceApi {

    override suspend fun updateCurrentFirebaseInstallationId(
        request: UpdateFirebaseInstallationIdRequest,
    ): ApiResult<Unit> {
        return safeAuthenticatedUnitCall(
            authSessionCoordinator = authSessionCoordinator,
        ) {
            client.put(
                "devices/current/firebase-installation-id",
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}