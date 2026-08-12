package eu.vitamo.app.infrastructure.notification

import eu.vitamo.app.infrastructure.permissions.GrantPermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import org.koin.dsl.module

val notificationKoinModule = module {

    single<PermissionManager> {
        GrantPermissionManager(
            grantManager = get(),
        )
    }

    single {
        LocalNotificationManager(
            permissionManager = get(),
            platformNotificationManager = get(),
        )
    }
}