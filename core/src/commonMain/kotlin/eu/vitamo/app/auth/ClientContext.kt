package eu.vitamo.app.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlin.uuid.Uuid

@Serializable
data class ClientContext(
    @Contextual
    val clientInstanceId: Uuid,
    val clientType: ClientType,
    val deviceName: String? = null,
    val platform: ClientPlatform,
    val browserName: String? = null,
    val appVersion: String? = null,
)

@Serializable
enum class ClientType {
    APP,
    WEB,
    DESKTOP,
    BACKEND,
}

@Serializable
enum class ClientPlatform {
    ANDROID,
    IOS,
    WEB,
    WINDOWS,
    MACOS,
    LINUX,
}
