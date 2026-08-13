package eu.vitamo.app.features.device.usecase

import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UpdateFirebaseInstallationIdUseCase(
    private val deviceRepository: DeviceRepository,
) {

    suspend operator fun invoke(
        deviceId: Uuid,
        firebaseInstallationId: String?,
    ): RepositoryResult<Unit> {
        val normalizedInstallationId =
            firebaseInstallationId?.trim()

        if (
            normalizedInstallationId != null &&
            normalizedInstallationId.isEmpty()
        ) {
            return RepositoryResult.Error(
                RepositoryError.BadRequest(
                    message =
                        "Firebase installation ID must not be blank.",
                ),
            )
        }

        return deviceRepository.updateFirebaseInstallationId(
            deviceId = deviceId,
            firebaseInstallationId =
                normalizedInstallationId,
        )
    }
}