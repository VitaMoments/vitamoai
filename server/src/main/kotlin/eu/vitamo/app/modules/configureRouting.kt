package eu.vitamo.app.modules

import eu.vitamo.app.features.auth.routes.authRoutes
import eu.vitamo.app.features.user.routes.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting() {
    routing {
        val uploadsDir = System.getenv("UPLOADS_DIR") ?: "uploads"
        staticFiles("/uploads", File(uploadsDir))

        get("/") {
            application.log.info("route hit")
            call.respondText("Hello from VitaMo Server")
        }
        apiRoutes()
    }
}

private fun Routing.apiRoutes() {
    route("/api") {
        authRoutes()

        authenticate("cookie-jwt-authentication") {
            userRoutes()
        }
    }
}