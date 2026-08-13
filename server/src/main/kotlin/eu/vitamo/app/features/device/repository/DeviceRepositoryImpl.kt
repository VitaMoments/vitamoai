package eu.vitamo.app.features.device.repository

import com.google.cloud.firestore.pipeline.stages.Where
import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.device.entity.DeviceEntity
import eu.vitamo.app.features.device.mapper.toRecord
import eu.vitamo.app.features.device.model.DeviceRecord
import eu.vitamo.app.features.device.table.DevicesTable
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.and
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull

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
        val device = DeviceEntity.findById(deviceId)

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

    override suspend fun findActiveByUserId(
        userId: Uuid,
    ): RepositoryResult<List<DeviceRecord>> = dbQuery {
        val devices = DeviceEntity.find {
            (DevicesTable.userId eq
                EntityID(
                    userId,
                    UsersTable,
                )) and
                DevicesTable.deletedAt.isNull() and
                DevicesTable.firebaseInstallationId.isNotNull()
            }.map { device -> device.toRecord() }
        RepositoryResult.Success(
            devices,
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