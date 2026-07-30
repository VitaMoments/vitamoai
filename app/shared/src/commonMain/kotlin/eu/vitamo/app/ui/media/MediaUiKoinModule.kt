package eu.vitamo.app.ui.media

import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.network.DevNetworkConfig
import eu.vitamo.app.ui.user.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val MediaUiKoinModule = module {
    single { MediaUrlResolver(DevNetworkConfig.API_BASE_URL ) }

    viewModelOf(::ProfileViewModel)
}