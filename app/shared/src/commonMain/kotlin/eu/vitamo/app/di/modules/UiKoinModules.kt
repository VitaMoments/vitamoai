package eu.vitamo.app.di.modules

import eu.vitamo.app.ui.auth.AuthUiKoinModule
import eu.vitamo.app.ui.user.UserUiKoinModule
import org.koin.core.module.Module

internal val uiKoinModules: List<Module> = listOf(
    AuthUiKoinModule,
    UserUiKoinModule
)