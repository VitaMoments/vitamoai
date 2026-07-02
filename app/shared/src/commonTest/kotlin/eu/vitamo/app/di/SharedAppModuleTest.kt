package eu.vitamo.app.di

import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertSame

class SharedAppModuleTest {

    @Test
    fun sharedAppModule_resolvesAuthApiConfigSingleton() {
        val koinApp = koinApplication {
            modules(sharedAppModule)
        }

        val first = koinApp.koin.get<eu.vitamo.app.auth.api.AuthApiConfig>()
        val second = koinApp.koin.get<eu.vitamo.app.auth.api.AuthApiConfig>()

        assertSame(first, second)
        koinApp.close()
    }
}
