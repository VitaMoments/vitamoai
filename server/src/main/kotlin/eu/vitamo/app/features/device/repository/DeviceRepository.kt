package eu.vitamo.app.features.device.repository

import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.features.device.model.DeviceRecord
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface DeviceRepository {
    suspend fun upsert(
        userId: Uuid,
        context: ClientContext,
        firebaseInstallationId: String?,
    ): RepositoryResult<DeviceRecord>

    suspend fun findById(
        deviceId: Uuid,
    ): RepositoryResult<DeviceRecord>

    suspend fun findActiveByUserId(
        userId: Uuid
    ) : RepositoryResult<List<DeviceRecord>>

    suspend fun updateFirebaseInstallationId(
        deviceId: Uuid,
        firebaseInstallationId: String?,
    ): RepositoryResult<Unit>
}