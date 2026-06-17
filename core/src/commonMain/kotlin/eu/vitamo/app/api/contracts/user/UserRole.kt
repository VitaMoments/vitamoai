package eu.vitamo.app.api.contracts.user

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    USER,
    ADMIN,
}

