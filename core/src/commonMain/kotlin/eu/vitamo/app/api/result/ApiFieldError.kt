package eu.vitamo.app.api.result

import kotlinx.serialization.Serializable

@Serializable
data class ApiFieldError(
    val field: String,
    val message: String,
)