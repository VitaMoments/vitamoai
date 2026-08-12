package eu.vitamo.app.api.contracts.device

import kotlinx.serialization.Serializable

@Serializable
enum class ClientType {
    APP,
    BROWSER
}