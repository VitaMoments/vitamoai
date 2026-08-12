package eu.vitamo.app.features.device.entity

import eu.vitamo.app.features.device.table.DevicesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class DeviceEntity(id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<DeviceEntity>(DevicesTable)

    var userId by DevicesTable.userId

    var clientType by DevicesTable.clientType
    var clientName by DevicesTable.clientName
    var clientVersion by DevicesTable.clientVersion

    var platform by DevicesTable.platform
    var osVersion by DevicesTable.osVersion

    var deviceName by DevicesTable.deviceName
    var deviceModel by DevicesTable.deviceModel

    var fcmToken by DevicesTable.fcmToken

    var lastSeenAt by DevicesTable.lastSeenAt
    var createdAt by DevicesTable.createdAt
    var updatedAt by DevicesTable.updatedAt
    var deletedAt by DevicesTable.deletedAt
}