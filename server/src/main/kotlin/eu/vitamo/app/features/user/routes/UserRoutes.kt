package eu.vitamo.app.features.user.routes

import eu.vitamo.app.features.user.usecase.SearchUsersUseCase
import eu.vitamo.app.infrastructure.network.helpers.getPaginationParameters
import eu.vitamo.app.infrastructure.network.helpers.getQueryParameter
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireQueryParameter
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val searchUsersUseCase: SearchUsersUseCase by inject()

    route("/users") {
        get() {
            val currentUserId = call.requireUserId()
            val query = call.getQueryParameter("query")
            val (limit, offset) = call.getPaginationParameters()

            val result = searchUsersUseCase(currentUserId, query, limit, offset.toLong())

            call.handleResult(result)
        }
    }
}