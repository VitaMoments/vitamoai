package eu.vitamo.app.api.result

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val status: Int? = null
)
