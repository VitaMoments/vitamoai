package eu.vitamo.app.infrastructure.permissions

import dev.brewkits.grant.AppGrant
import dev.brewkits.grant.GrantManager
import dev.brewkits.grant.GrantStatus

class GrantPermissionManager(
    private val grantManager: GrantManager,
) : PermissionManager {

    override suspend fun check(
        permission: AppPermission,
    ): PermissionStatus {
        return grantManager
            .checkStatus(permission.toGrant())
            .toPermissionStatus()
    }

    override suspend fun request(
        permission: AppPermission,
    ): PermissionStatus {
        return grantManager
            .request(permission.toGrant())
            .toPermissionStatus()
    }

    override fun openSettings() {
        grantManager.openSettings()
    }
}

private fun AppPermission.toGrant(): AppGrant =
    when (this) {
        AppPermission.NOTIFICATIONS ->
            AppGrant.NOTIFICATION
    }

private fun GrantStatus.toPermissionStatus(): PermissionStatus =
    when (this) {
        GrantStatus.NOT_DETERMINED ->
            PermissionStatus.NOT_DETERMINED

        GrantStatus.GRANTED ->
            PermissionStatus.GRANTED

        GrantStatus.DENIED ->
            PermissionStatus.DENIED

        GrantStatus.DENIED_ALWAYS ->
            PermissionStatus.DENIED_ALWAYS

        GrantStatus.PARTIAL_GRANTED ->
            PermissionStatus.PARTIAL_GRANTED

        GrantStatus.BUSY ->
            PermissionStatus.BUSY
    }
