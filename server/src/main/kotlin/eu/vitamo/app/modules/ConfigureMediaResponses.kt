package eu.vitamo.app.modules

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.partialcontent.PartialContent

fun Application.configureMediaResponses() {
    install(PartialContent)
}