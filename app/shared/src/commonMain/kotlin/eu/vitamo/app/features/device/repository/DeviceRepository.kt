package eu.vitamo.app.features.device.repository

import eu.vitamo.app.repository.RepositoryResult

interface DeviceRepository {

    suspend fun updateCurrentFirebaseInstallationId(
        firebaseInstallationId: String?,
    ): RepositoryResult<Unit>
}