package eu.vitamo.app.features.device.api

import eu.vitamo.app.api.contracts.device.UpdateFirebaseInstallationIdRequest
import eu.vitamo.app.api.result.ApiResult

interface DeviceApi {

    suspend fun updateCurrentFirebaseInstallationId(
        request: UpdateFirebaseInstallationIdRequest,
    ): ApiResult<Unit>
}