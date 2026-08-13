package eu.vitamo.app.features.device.mapper

import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.features.device.entity.DeviceEntity
import eu.vitamo.app.features.device.model.DeviceRecord

fun DeviceEntity.toClientContext(): ClientContext {
    return ClientContext(
        clientInstanceId = id.value,
        clientType = clientType,
        clientName = clientName,
        clientVersion = clientVersion,
        platform = platform,
        osVersion = osVersion,
        deviceName = deviceName,
        deviceModel = deviceModel,
    )
}

fun DeviceEntity.toRecord(): DeviceRecord =
    DeviceRecord(
        id = id.value,
        userId = userId.value,

        clientType = clientType,
        clientName = clientName,
        clientVersion = clientVersion,

        platform = platform,
        osVersion = osVersion,

        deviceName = deviceName,
        deviceModel = deviceModel,

        firebaseInstallationId = firebaseInstallationId,

        lastSeenAt = lastSeenAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )