package eu.vitamo.app.ui.auth.password_recovery.reset_password

sealed interface ResetPasswordEvent {
    data class TokenLoaded(val token: String) : ResetPasswordEvent
    data class EmailChanged(val value: String) : ResetPasswordEvent
    data class PasswordChanged(val value: String) : ResetPasswordEvent
    data class RepeatPasswordChanged(val value: String) : ResetPasswordEvent
    data object Submit : ResetPasswordEvent
    data object ClearMessage : ResetPasswordEvent
}