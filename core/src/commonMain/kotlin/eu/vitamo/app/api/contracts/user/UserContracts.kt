package eu.vitamo.app.api.contracts.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UpdateProfileImageRequest(
    @Contextual
    val mediaId: Uuid,
)