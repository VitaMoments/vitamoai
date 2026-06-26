package eu.vitamo.app.ui.auth.password_recovery.forgot_password

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
    data object ClearMessage : ForgotPasswordEvent
}