package eu.vitamo.app.ui.user

import eu.vitamo.app.ui.user.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val UserUiKoinModule = module {
    viewModelOf(::ProfileViewModel)
}