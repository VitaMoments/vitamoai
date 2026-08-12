package eu.vitamo.app.features.device.provider

import eu.vitamo.app.api.contracts.device.ClientPlatform
import eu.vitamo.app.api.contracts.device.ClientType

data class PlatformClientInfo(
    val clientType: ClientType,
    val clientName: String,
    val clientVersion: String?,
    val platform: ClientPlatform,
    val osVersion: String?,
    val deviceName: String?,
    val deviceModel: String?,
)

expect class PlatformClientInfoProvider() {
    fun get(): PlatformClientInfo
}