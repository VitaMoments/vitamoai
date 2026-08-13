package eu.vitamo.app.features.device.service

import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.infrastructure.storage.FirebaseInstallationIdStorage
import eu.vitamo.app.repository.RepositoryResult

class FirebaseInstallationIdSynchronizer(
    private val firebaseInstallationIdStorage:
    FirebaseInstallationIdStorage,
    private val deviceRepository: DeviceRepository,
) {

    suspend fun synchronizeStored():
            RepositoryResult<Unit> {
        val firebaseInstallationId =
            firebaseInstallationIdStorage
                .get()
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: return RepositoryResult.Success(Unit)

        return deviceRepository.updateCurrentFirebaseInstallationId(
            firebaseInstallationId =
                firebaseInstallationId,
        )
    }

    suspend fun clearRemote():
            RepositoryResult<Unit> {
        return deviceRepository.updateCurrentFirebaseInstallationId(
            firebaseInstallationId = null,
        )
    }
}