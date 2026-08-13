package eu.vitamo.app.di

import eu.vitamo.app.infrastructure.di.sharedAppModule
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertSame

class SharedAppModuleTest {

    @Test
    fun sharedAppModule_resolvesAuthApiConfigSingleton() {
        val koinApp = koinApplication {
            modules(sharedAppModule)
        }

        val first = koinApp.koin.get<AuthApiConfig>()
        val second = koinApp.koin.get<AuthApiConfig>()

        assertSame(first, second)
        koinApp.close()
    }
}
