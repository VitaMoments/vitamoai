package eu.vitamo.app.features.user.routes

import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.features.media.usecase.UpdateProfileImageUseCase
import eu.vitamo.app.features.user.routes.helper.receiveProfileImageUpload
import eu.vitamo.app.features.user.usecase.GetUserUseCase
import eu.vitamo.app.features.user.usecase.SearchUsersUseCase
import eu.vitamo.app.infrastructure.network.helpers.getPaginationParameters
import eu.vitamo.app.infrastructure.network.helpers.getQueryParameter
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireQueryParameter
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import eu.vitamo.app.infrastructure.network.helpers.respondRepositoryError
import eu.vitamo.app.repository.RepositoryResult
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun Route.userRoutes() {
    val searchUsersUseCase: SearchUsersUseCase by inject()
    val getUserUseCase: GetUserUseCase by inject()
    val updateProfileImageUseCase: UpdateProfileImageUseCase by inject()

    route("/users") {
        get() {
            val currentUserId = call.requireUserId()
            val query = call.getQueryParameter("query")
            val (limit, offset) = call.getPaginationParameters()

            val result = searchUsersUseCase(currentUserId, query, limit, offset.toLong())

            call.handleResult(result)
        }

        get("/{userId}") {
            val currentUserId = call.requireUserId()

            val userId = call.parameters["userId"]
                ?.let { value ->
                    runCatching { Uuid.parse(value) }.getOrNull()
                }
                ?: throw ApiException.BadRequest(
                    message = "Invalid or missing userId.",
                )

            val result = getUserUseCase(
                currentUserId = currentUserId,
                userId = userId,
            )

            call.handleResult(result)
        }

        get("/me") {
            val currentUserId = call.requireUserId()
            val result = getUserUseCase(
                currentUserId = currentUserId,
                userId = currentUserId
            )
            call.handleResult(result)
        }

        put("/me/profile-image") {
            val currentUserId = call.requireUserId()

            when (
                val uploadResult =
                    call.receiveProfileImageUpload()
            ) {
                is RepositoryResult.Success -> {
                    call.handleResult(
                        result = updateProfileImageUseCase(
                            currentUserId = currentUserId,
                            source = uploadResult.data.temporaryFile,
                        ),
                    )
                }

                is RepositoryResult.Error -> {
                    call.respondRepositoryError(
                        error = uploadResult.error,
                    )
                }
            }
        }
    }
}