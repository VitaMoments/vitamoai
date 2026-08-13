package eu.vitamo.app.features.device.repository

import eu.vitamo.app.api.contracts.device.UpdateFirebaseInstallationIdRequest
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.features.device.api.DeviceApi
import eu.vitamo.app.mapper.toRepositoryError
import eu.vitamo.app.repository.RepositoryResult

class DeviceRepositoryImpl(
    private val deviceApi: DeviceApi,
) : DeviceRepository {

    override suspend fun updateCurrentFirebaseInstallationId(
        firebaseInstallationId: String?,
    ): RepositoryResult<Unit> {
        val result =
            deviceApi.updateCurrentFirebaseInstallationId(
                request = UpdateFirebaseInstallationIdRequest(
                    firebaseInstallationId =
                        firebaseInstallationId,
                ),
            )

        return when (result) {
            is ApiResult.Success -> {
                RepositoryResult.Success(Unit)
            }

            is ApiResult.Error -> {
                RepositoryResult.Error(
                    error = result.error.toRepositoryError(),
                )
            }
        }
    }
}