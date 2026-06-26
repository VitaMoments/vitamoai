package eu.vitamo.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthDestination : AppDestination {
    override val title: String?

    @Serializable
    data object Login : AuthDestination {
        override val route = "login"
        override val title = "Login"
    }

    @Serializable
    data object Register : AuthDestination {
        override val route = "register"
        override val title = "Register"
    }

    @Serializable
    data class VerifyEmailAddress(val emailAddress: String) : AuthDestination {
        override val route = "verify_email"
        override val title = "Verify Email"
    }

    @Serializable
    data object ForgotPassword : AuthDestination {
        override val route = "forgot_password"
        override val title = "Forgot Password"
    }

    @Serializable
    data class ResetPassword(
        val token: String
    ) : AuthDestination {
        override val route = "reset_password"
        override val title = "Reset Password"
    }
}
