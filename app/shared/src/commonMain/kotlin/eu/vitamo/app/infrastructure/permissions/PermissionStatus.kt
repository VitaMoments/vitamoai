package eu.vitamo.app.infrastructure.permissions

enum class PermissionStatus {
    NOT_DETERMINED,
    PARTIAL_GRANTED,
    GRANTED,
    DENIED,
    DENIED_ALWAYS,
    BUSY
}