package eu.vitamo.app.infrastructure.permissions.notification

sealed interface NotificationPermissionEvent {

    data object Refresh : NotificationPermissionEvent

    data object RequestPermission : NotificationPermissionEvent

    data object OpenSettings : NotificationPermissionEvent
}