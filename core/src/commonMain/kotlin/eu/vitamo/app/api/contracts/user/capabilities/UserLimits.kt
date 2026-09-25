package eu.vitamo.app.api.contracts.user.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class UserLimits(
    val maxImagesPerPost: Int,
    val maxPostLength: Int,
)