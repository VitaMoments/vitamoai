package eu.vitamo.app.api.contracts.user.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class UserCapabilities(
    val actions: Set<UserAction>,
    val limits: UserLimits
)
