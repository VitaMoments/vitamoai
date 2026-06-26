package eu.vitamo.app.ui.auth.login

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent

    data class EmailNotVerified(
        val emailAddress: String,
    ) : LoginEvent
}