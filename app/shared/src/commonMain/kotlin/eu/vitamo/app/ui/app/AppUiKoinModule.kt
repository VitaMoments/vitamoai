package eu.vitamo.app.ui.app


import com.russhwolf.settings.Settings
import eu.vitamo.app.infrastructure.storage.AppPreferencesStorage
import eu.vitamo.app.infrastructure.storage.AppPreferencesStorageImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppUiKoinModule = module {
    single<Settings> {
        Settings()
    }

    single<AppPreferencesStorage> {
        AppPreferencesStorageImpl(
            settings = get(),
        )
    }

    viewModelOf(::AppViewModel)
}