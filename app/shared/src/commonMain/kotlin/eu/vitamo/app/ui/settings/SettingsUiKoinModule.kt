package eu.vitamo.app.ui.settings

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val SettingsUiKoinModule = module {
    viewModelOf(::SettingsViewModel)
}