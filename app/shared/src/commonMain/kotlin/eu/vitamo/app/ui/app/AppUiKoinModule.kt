package eu.vitamo.app.ui.app

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppUiKoinModule = module {
    viewModelOf(::AppViewModel)
}