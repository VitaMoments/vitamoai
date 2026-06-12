package eu.vitamo.app.di

import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerModuleTest {

    @Test
    fun serverModule_resolvesExpectedGreeting() {
        val koinApp = koinApplication {
            modules(serverModule)
        }

        val greeting = koinApp.koin.get<String>()

        assertEquals("Hello, server!", greeting)
        koinApp.close()
    }
}

