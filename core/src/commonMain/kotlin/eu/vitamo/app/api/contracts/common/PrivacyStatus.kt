package eu.vitamo.app.api.contracts.common

import kotlinx.serialization.Serializable

@Serializable
enum class PrivacyStatus {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE,
}