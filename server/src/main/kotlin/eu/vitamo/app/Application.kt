package eu.vitamo.app

import eu.vitamo.app.modules.configureHTTP
import eu.vitamo.app.modules.configureKoin
import eu.vitamo.app.modules.configureSecurity
import eu.vitamo.app.modules.configureSerialization
import eu.vitamo.app.modules.configureStatusPages
import eu.vitamo.app.features.auth.routes.authRoutes
import eu.vitamo.app.features.feed.routes.feedRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureHTTP()
    configureSerialization()
    configureStatusPages()
    configureSecurity()

    routing {
        route("/api") {
            authRoutes()
            route("/v1") {
                feedRoutes()
            }
        }
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
    }
}
