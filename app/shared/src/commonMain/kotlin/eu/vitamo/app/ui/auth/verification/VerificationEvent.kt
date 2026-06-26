package eu.vitamo.app.ui.auth.verification

sealed interface VerifyEmailEvent {
    data object VerificationSuccess : VerifyEmailEvent
}