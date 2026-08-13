package eu.vitamo.app.infrastructure.di.modules

import eu.vitamo.app.features.auth.authModule
import eu.vitamo.app.features.device.deviceModule
import eu.vitamo.app.features.media.mediaModule
import eu.vitamo.app.features.settings.settingsModule
import eu.vitamo.app.features.user.userModule
import org.koin.core.module.Module

internal val koinModules: List<Module> = listOf(
    userModule,
    settingsModule,
    mediaModule,
    deviceModule,
    authModule
)