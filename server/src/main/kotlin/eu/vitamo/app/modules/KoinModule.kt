package eu.vitamo.app.modules

import eu.vitamo.app.di.initServerKoin
import io.ktor.server.application.Application

fun Application.configureKoin() {
    initServerKoin()
}

