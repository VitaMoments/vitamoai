package eu.vitamo.app

import android.app.Application
import eu.vitamo.app.infrastructure.di.initKoin
import eu.vitamo.app.network.auth.initializeAuthCookiePersistence
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class VitaMoApplication : Application() {

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
    }
}