package eu.vitamo.app

import eu.vitamo.app.di.initServerKoin
import eu.vitamo.app.modules.configureSerialization
import eu.vitamo.app.modules.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    initServerKoin()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
    }
}