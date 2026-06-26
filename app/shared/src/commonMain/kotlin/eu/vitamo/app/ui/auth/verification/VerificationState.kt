package eu.vitamo.app.ui.auth.verification

data class VerificationState(
    val code: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val codeError: String? = null,
    val generalError: String? = null,
    val message: String? = null,
)