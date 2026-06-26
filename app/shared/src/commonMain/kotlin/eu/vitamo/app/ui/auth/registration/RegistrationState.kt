package eu.vitamo.app.ui.auth.registration

data class RegistrationState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val alias: String = "",
    val birthDate: String = "",

    val isLoading: Boolean = false,

    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val displayNameError: String? = null,
    val birthDateError: String? = null,
    val generalError: String? = null,
)