package eu.vitamo.app.features.device.repository

import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.device.entity.DeviceEntity
import eu.vitamo.app.features.device.mapper.toRecord
import eu.vitamo.app.features.device.model.DeviceRecord
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.dao.id.EntityID

class DeviceRepositoryImpl : DeviceRepository {

    override suspend fun upsert(
        userId: Uuid,
        context: ClientContext,
        firebaseInstallationId: String?,
    ): RepositoryResult<DeviceRecord> = dbQuery {
        val now =
            Clock.System.now().epochSeconds

        val existing =
            DeviceEntity.findById(
                context.clientInstanceId,
            )

        val device = if (existing == null) {
            DeviceEntity.new(
                id = context.clientInstanceId,
            ) {
                this.userId =
                    EntityID(
                        userId,
                        UsersTable,
                    )

                applyContext(
                    context = context,
                    now = now,
                )

                createdAt = now
                updatedAt = now
                lastSeenAt = now
                deletedAt = null

                this.firebaseInstallationId =
                    firebaseInstallationId
            }
        } else {
            if (existing.userId.value != userId) {
                return@dbQuery RepositoryResult.Error(
                    RepositoryError.Conflict(
                        message =
                            "Device is already registered to another user.",
                    ),
                )
            }

            existing.applyContext(
                context = context,
                now = now,
            )

            if (firebaseInstallationId != null) {
                existing.firebaseInstallationId =
                    firebaseInstallationId
            }

            existing
        }

        RepositoryResult.Success(
            device.toRecord(),
        )
    }

    override suspend fun findById(
        deviceId: Uuid,
    ): RepositoryResult<DeviceRecord> = dbQuery {
        val device =
            DeviceEntity.findById(deviceId)

        if (
            device == null ||
            device.deletedAt != null
        ) {
            return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Device not found.",
                ),
            )
        }

        RepositoryResult.Success(
            device.toRecord(),
        )
    }

    override suspend fun updateFirebaseInstallationId(
        deviceId: Uuid,
        firebaseInstallationId: String?,
    ): RepositoryResult<Unit> = dbQuery {
        val device =
            DeviceEntity.findById(
                deviceId,
            )

        if (
            device == null ||
            device.deletedAt != null
        ) {
            return@dbQuery RepositoryResult.Error(
                RepositoryError.NotFound(
                    message = "Device not found.",
                ),
            )
        }

        val now =
            Clock.System.now().epochSeconds

        device.firebaseInstallationId =
            firebaseInstallationId

        device.updatedAt = now
        device.lastSeenAt = now

        RepositoryResult.Success(Unit)
    }

    private fun DeviceEntity.applyContext(
        context: ClientContext,
        now: Long,
    ) {
        clientType = context.clientType
        clientName = context.clientName
        clientVersion = context.clientVersion

        platform = context.platform
        osVersion = context.osVersion

        deviceName = context.deviceName
        deviceModel = context.deviceModel

        lastSeenAt = now
        updatedAt = now
        deletedAt = null
    }
}