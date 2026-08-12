package eu.vitamo.app.api.contracts.device

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ClientContext(
    val clientInstanceId: Uuid,
    val clientType: ClientType,
    val clientName: String,
    val clientVersion: String? = null,
    val platform: ClientPlatform,
    val osVersion: String? = null,
    val deviceName: String? = null,
    val deviceModel: String? = null,
)