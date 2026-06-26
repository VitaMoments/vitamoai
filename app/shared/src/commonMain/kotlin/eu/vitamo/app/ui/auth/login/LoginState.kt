package eu.vitamo.app.ui.auth.login

data class LoginState(
    val emailAddress: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailAddressError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
)