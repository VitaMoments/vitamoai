package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.DeleteFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedPageUseCase
import eu.vitamo.app.features.feed.usecase.UpdateFeedItemUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun Route.feedRoutes() {
    val createUseCase: CreateFeedItemUseCase by inject()
    val getItemUseCase: GetFeedItemUseCase by inject()
    val getPageUseCase: GetFeedPageUseCase by inject()
    val updateUseCase: UpdateFeedItemUseCase by inject()
    val deleteUseCase: DeleteFeedItemUseCase by inject()

    authenticate("cookie-jwt-authentication") {
        route("/feed") {
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateFeedItemRequest>()
                val created = createUseCase(userId, request)
                call.respond(HttpStatusCode.Created, created)
            }

            get("/{uuid}") {
                val userId = call.requireUserId()
                val feedItemId = call.parameters["uuid"]?.let(Uuid::parse) ?: throw FeedException.NotFound()
                call.respond(getItemUseCase(userId, feedItemId))
            }

            get("/me") {
                val userId = call.requireUserId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                call.respond(getPageUseCase.myFeed(userId, limit, offset))
            }

            get {
                val userId = call.requireUserId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val categories = call.request.queryParameters["categories"]
                    ?.split(",")
                    ?.mapNotNull { value ->
                        runCatching { FeedCategory.valueOf(value.trim()) }.getOrNull()
                    }
                    ?.toSet()
                    ?: emptySet()

                call.respond(getPageUseCase.generalFeed(userId, limit, offset, categories))
            }

            patch("/{uuid}") {
                val userId = call.requireUserId()
                val feedItemId = call.parameters["uuid"]?.let(Uuid::parse) ?: throw FeedException.NotFound()
                val request = call.receive<UpdateFeedItemRequest>()
                call.respond(updateUseCase(userId, feedItemId, request))
            }

            delete("/{uuid}") {
                val userId = call.requireUserId()
                val feedItemId = call.parameters["uuid"]?.let(Uuid::parse) ?: throw FeedException.NotFound()
                deleteUseCase(userId, feedItemId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.requireUserId(): Uuid {
    val principal = principal<JWTPrincipal>()
        ?: throw FeedException.Forbidden("Authentication required")
    val userId = principal.payload.getClaim(JWTConfig.USER_ID_CLAIM).asString()
        ?: throw FeedException.Forbidden("Authentication required")
    return Uuid.parse(userId)
}
