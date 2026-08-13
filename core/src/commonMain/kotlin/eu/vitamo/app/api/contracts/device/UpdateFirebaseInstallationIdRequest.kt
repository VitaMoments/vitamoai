package eu.vitamo.app.api.contracts.device

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFirebaseInstallationIdRequest(
    val firebaseInstallationId: String?,
)