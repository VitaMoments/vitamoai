package eu.vitamo.app.ui.auth.password_recovery.reset_password

data class ResetPasswordState(
    val token: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resetCompleted: Boolean = false,
)