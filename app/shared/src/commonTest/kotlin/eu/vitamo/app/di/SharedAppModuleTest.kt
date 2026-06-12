package eu.vitamo.app.di

import eu.vitamo.app.Greeting
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertSame

class SharedAppModuleTest {

    @Test
    fun sharedAppModule_resolvesGreetingSingleton() {
        val koinApp = koinApplication {
            modules(sharedAppModule)
        }

        val first = koinApp.koin.get<Greeting>()
        val second = koinApp.koin.get<Greeting>()

        assertSame(first, second)
        koinApp.close()
    }
}

