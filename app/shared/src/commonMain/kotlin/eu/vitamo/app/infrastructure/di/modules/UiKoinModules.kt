package eu.vitamo.app.infrastructure.di.modules

import eu.vitamo.app.ui.app.AppUiKoinModule
import eu.vitamo.app.ui.auth.AuthUiKoinModule
import eu.vitamo.app.ui.feed.feedUiKoinModule
import eu.vitamo.app.ui.media.MediaUiKoinModule
import eu.vitamo.app.ui.settings.SettingsUiKoinModule
import eu.vitamo.app.ui.user.UserUiKoinModule
import org.koin.core.module.Module

internal val uiKoinModules: List<Module> = listOf(
    AuthUiKoinModule,
    UserUiKoinModule,
    MediaUiKoinModule,
    AppUiKoinModule,
    SettingsUiKoinModule,
    feedUiKoinModule
)