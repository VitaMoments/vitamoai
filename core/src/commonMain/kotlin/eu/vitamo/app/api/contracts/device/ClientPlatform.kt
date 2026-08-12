package eu.vitamo.app.api.contracts.device

import kotlinx.serialization.Serializable

@Serializable
enum class ClientPlatform {
    ANDROID,
    IOS,
    MACOS,
    WINDOWS,
    LINUX,
    CHROMEOS,
    UNKNOWN
}