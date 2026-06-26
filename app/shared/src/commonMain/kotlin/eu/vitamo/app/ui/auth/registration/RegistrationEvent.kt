package eu.vitamo.app.ui.auth.registration

sealed interface RegistrationEvent {
    data class RegistrationSuccess(
        val email: String,
    ) : RegistrationEvent
}