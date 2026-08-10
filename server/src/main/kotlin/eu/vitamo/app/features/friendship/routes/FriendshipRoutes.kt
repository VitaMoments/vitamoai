package eu.vitamo.app.features.friendship.routes

import eu.vitamo.app.api.contracts.friendship.SendFriendRequestRequest
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.features.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.DeleteFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.GetFriendsUseCase
import eu.vitamo.app.features.friendship.usecase.GetIncomingFriendRequestsUseCase
import eu.vitamo.app.features.friendship.usecase.GetOutgoingFriendRequestsUseCase
import eu.vitamo.app.features.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.infrastructure.network.helpers.handleResult
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun Route.friendshipRoutes(

) {
    val sendFriendRequestUseCase : SendFriendRequestUseCase by inject()
    val acceptFriendRequestUseCase: AcceptFriendRequestUseCase by inject()
    val deleteFriendRequestUseCase: DeleteFriendRequestUseCase by inject()
    val removeFriendshipUseCase: RemoveFriendshipUseCase by inject()
    val getIncomingFriendRequestsUseCase: GetIncomingFriendRequestsUseCase by inject()
    val getOutgoingFriendRequestsUseCase: GetOutgoingFriendRequestsUseCase by inject()
    val getFriendsUseCase: GetFriendsUseCase by inject()

    route("/friendships") {
        post("/requests") {
            val currentUserId =
                call.requireUserId()

            val request =
                call.receive<SendFriendRequestRequest>()

            call.handleResult(
                result =
                    sendFriendRequestUseCase(
                        currentUserId =
                            currentUserId,
                        targetUserId =
                            request.targetUserId,
                    ),
                successStatusCode =
                    HttpStatusCode.Created,
            )
        }

        get("/requests/incoming") {
            val currentUserId =
                call.requireUserId()

            val pagination =
                call.friendshipPagination()

            call.handleResult(
                result =
                    getIncomingFriendRequestsUseCase(
                        currentUserId =
                            currentUserId,
                        limit =
                            pagination.limit,
                        offset =
                            pagination.offset,
                    ),
            )
        }

        get("/requests/outgoing") {
            val currentUserId =
                call.requireUserId()

            val pagination =
                call.friendshipPagination()

            call.handleResult(
                result =
                    getOutgoingFriendRequestsUseCase(
                        currentUserId =
                            currentUserId,
                        limit =
                            pagination.limit,
                        offset =
                            pagination.offset,
                    ),
            )
        }

        post(
            "/requests/{friendshipId}/accept",
        ) {
            val currentUserId =
                call.requireUserId()

            val friendshipId = call.parameters["friendshipId"]
                ?.let {
                    runCatching {
                        Uuid.parse(it)
                    }.getOrNull() } ?: throw ApiException.BadRequest("Missing friendshipId as parameter")

            call.handleResult(
                result =
                    acceptFriendRequestUseCase(
                        currentUserId =
                            currentUserId,
                        friendshipId =
                            friendshipId,
                    ),
            )
        }

        delete(
            "/requests/{friendshipId}",
        ) {
            val currentUserId =
                call.requireUserId()

            val friendshipId = call.parameters["friendshipId"]
                ?.let {
                    runCatching {
                        Uuid.parse(it)
                    }.getOrNull() } ?: throw ApiException.BadRequest("Missing friendshipId as parameter")


            call.handleResult(
                result =
                    deleteFriendRequestUseCase(
                        currentUserId =
                            currentUserId,
                        friendshipId =
                            friendshipId,
                    ),
            )
        }

        get {
            val currentUserId =
                call.requireUserId()

            val pagination =
                call.friendshipPagination()

            call.handleResult(
                result = getFriendsUseCase(
                    currentUserId =
                        currentUserId,
                    limit =
                        pagination.limit,
                    offset =
                        pagination.offset,
                ),
            )
        }

        delete("/{friendshipId}") {
            val currentUserId =
                call.requireUserId()

            val friendshipId = call.parameters["friendshipId"]
                ?.let {
                    runCatching {
                        Uuid.parse(it)
                    }.getOrNull() } ?: throw ApiException.BadRequest("Missing friendshipId as parameter")


            call.handleResult(
                result =
                    removeFriendshipUseCase(
                        currentUserId =
                            currentUserId,
                        friendshipId =
                            friendshipId,
                    ),
            )
        }
    }
}

private data class FriendshipPagination(
    val limit: Int,
    val offset: Long,
)

private fun io.ktor.server.application.ApplicationCall
        .friendshipPagination():
        FriendshipPagination {
    val limit = request
        .queryParameters["limit"]
        ?.toIntOrNull()
        ?.coerceIn(
            minimumValue = 1,
            maximumValue = 50,
        )
        ?: 20

    val offset = request
        .queryParameters["offset"]
        ?.toLongOrNull()
        ?.coerceAtLeast(0L)
        ?: 0L

    return FriendshipPagination(
        limit = limit,
        offset = offset,
    )
}