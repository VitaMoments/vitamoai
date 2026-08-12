package eu.vitamo.app.infrastructure.permissions.notification

sealed interface NotificationPermissionEffect {

    data object PermissionGranted :
        NotificationPermissionEffect

    data class ShowMessage(
        val message: String,
    ) : NotificationPermissionEffect
}