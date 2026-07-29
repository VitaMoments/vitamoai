package eu.vitamo.app.api.result

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: ErrorCode,
    val message: String = "No error message provided",
    val fieldErrors: List<ApiFieldError> = emptyList(),
    val traceId: String? = null,
    val status: Int,
)