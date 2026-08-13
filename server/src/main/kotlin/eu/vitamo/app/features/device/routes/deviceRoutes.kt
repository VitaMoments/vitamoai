package eu.vitamo.app.features.device.routes

import eu.vitamo.app.api.contracts.device.UpdateFirebaseInstallationIdRequest
import eu.vitamo.app.features.device.usecase.UpdateFirebaseInstallationIdUseCase
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireDeviceId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.deviceRoutes() {
    val updateFirebaseInstallationIdUseCase:
        UpdateFirebaseInstallationIdUseCase by inject()

    route("/devices") {
        authenticate("cookie-jwt-authentication") {
            put("/current/firebase-installation-id") {
                val deviceId =
                    call.requireDeviceId()

                val request =
                    call.receive<UpdateFirebaseInstallationIdRequest>()

                val result =
                    updateFirebaseInstallationIdUseCase(
                        deviceId = deviceId,
                        firebaseInstallationId =
                            request.firebaseInstallationId,
                    )

                call.handleResult(
                    result = result,
                    onSuccess = {
                        respond(
                            HttpStatusCode.NoContent,
                        )
                    },
                )
            }
        }
    }
}