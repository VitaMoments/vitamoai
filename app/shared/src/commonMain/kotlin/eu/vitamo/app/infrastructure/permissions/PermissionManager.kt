package eu.vitamo.app.infrastructure.permissions

interface PermissionManager {
    suspend fun check(
        permission: AppPermission,
    ): PermissionStatus

    suspend fun request(
        permission: AppPermission,
    ): PermissionStatus

    fun openSettings()
}