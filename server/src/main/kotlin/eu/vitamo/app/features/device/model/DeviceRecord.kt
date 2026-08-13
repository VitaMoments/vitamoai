package eu.vitamo.app.features.device.model

import eu.vitamo.app.api.contracts.device.ClientPlatform
import eu.vitamo.app.api.contracts.device.ClientType
import kotlin.uuid.Uuid

data class DeviceRecord(
    val id: Uuid,
    val userId: Uuid,

    val clientType: ClientType,
    val clientName: String,
    val clientVersion: String?,

    val platform: ClientPlatform,
    val osVersion: String?,

    val deviceName: String?,
    val deviceModel: String?,

    val firebaseInstallationId: String?,

    val lastSeenAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)