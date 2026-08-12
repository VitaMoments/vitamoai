package eu.vitamo.app

import android.app.Application
import eu.vitamo.app.infrastructure.app.AppInitializer
import eu.vitamo.app.infrastructure.di.initKoin
import eu.vitamo.app.network.auth.initializeAuthCookiePersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.get

class VitaMoApplication : Application() {
    private val applicationScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default,
        )

    override fun onCreate() {
        super.onCreate()

        initializeAuthCookiePersistence(
            applicationContext,
        )

        initKoin {
            androidLogger()

            androidContext(
                this@VitaMoApplication,
            )
        }
        applicationScope.launch {
            get()
                .get<AppInitializer>()
                .initialize()
        }
    }
}