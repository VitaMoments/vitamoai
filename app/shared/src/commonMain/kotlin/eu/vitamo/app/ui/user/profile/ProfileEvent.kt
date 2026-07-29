package eu.vitamo.app.ui.user.profile

sealed interface ProfileEvent {

    data class ShowMessage(
        val message: String,
    ) : ProfileEvent
}