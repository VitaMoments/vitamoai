package eu.vitamo.app.features.device.table

import eu.vitamo.app.api.contracts.device.ClientPlatform
import eu.vitamo.app.api.contracts.device.ClientType
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object DevicesTable : UuidTable("devices") {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("devices_user_id_idx")

    val clientType = enumerationByName<ClientType>(
        name = "client_type",
        length = 20,
    )

    val clientName = varchar(
        name = "client_name",
        length = 100,
    )

    val clientVersion = varchar(
        name = "client_version",
        length = 100,
    ).nullable()

    val platform = enumerationByName<ClientPlatform>(
        name = "platform",
        length = 20,
    )

    val osVersion = varchar(
        name = "os_version",
        length = 100,
    ).nullable()

    val deviceName = varchar(
        name = "device_name",
        length = 255,
    ).nullable()

    val deviceModel = varchar(
        name = "device_model",
        length = 255,
    ).nullable()

    val fcmToken = text(
        name = "fcm_token",
    ).nullable()

    val lastSeenAt = long(
        name = "last_seen_at_epoch_seconds",
    ).nullable()

    val createdAt = long(
        name = "created_at_epoch_seconds",
    )

    val updatedAt = long(
        name = "updated_at_epoch_seconds",
    )

    val deletedAt = long(
        name = "deleted_at_epoch_seconds",
    ).nullable()
}