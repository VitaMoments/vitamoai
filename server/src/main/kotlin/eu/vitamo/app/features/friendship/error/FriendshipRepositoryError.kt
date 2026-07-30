package eu.vitamo.app.features.friendship.error

import eu.vitamo.app.api.contracts.errorcodes.FriendshipErrorCode
import eu.vitamo.app.repository.RepositoryError

object FriendshipRepositoryErrors {
    fun cannotFriendSelf(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode.CANNOT_FRIEND_SELF,
            message =
                "Je kunt jezelf geen vriendschapsverzoek sturen.",
            status = HTTP_BAD_REQUEST,
        )
    }

    fun friendRequestAlreadyExists(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .FRIEND_REQUEST_ALREADY_EXISTS,
            message =
                "Je hebt deze gebruiker al een vriendschapsverzoek gestuurd.",
            status = HTTP_CONFLICT,
        )
    }

    fun incomingFriendRequestExists(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .INCOMING_FRIEND_REQUEST_EXISTS,
            message =
                "Je hebt al een vriendschapsverzoek van deze gebruiker ontvangen.",
            status = HTTP_CONFLICT,
        )
    }

    fun friendRequestNotFound(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .FRIEND_REQUEST_NOT_FOUND,
            message =
                "Het vriendschapsverzoek is niet gevonden.",
            status = HTTP_NOT_FOUND,
        )
    }

    fun friendRequestNotPending(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .FRIEND_REQUEST_NOT_PENDING,
            message =
                "Dit vriendschapsverzoek is niet meer actief.",
            status = HTTP_CONFLICT,
        )
    }

    fun notFriendRequestRecipient(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .NOT_FRIEND_REQUEST_RECIPIENT,
            message =
                "Alleen de ontvanger kan dit vriendschapsverzoek accepteren.",
            status = HTTP_FORBIDDEN,
        )
    }

    fun notFriendshipParticipant(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .NOT_FRIENDSHIP_PARTICIPANT,
            message =
                "Je bent geen deelnemer aan deze vriendschap.",
            status = HTTP_FORBIDDEN,
        )
    }

    fun alreadyFriends(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode.ALREADY_FRIENDS,
            message =
                "Jullie zijn al vrienden.",
            status = HTTP_CONFLICT,
        )
    }

    fun friendshipNotFound(): RepositoryError.Api {
        return apiError(
            code =
                FriendshipErrorCode
                    .FRIENDSHIP_NOT_FOUND,
            message =
                "De vriendschap is niet gevonden.",
            status = HTTP_NOT_FOUND,
        )
    }

    private fun apiError(
        code: FriendshipErrorCode,
        message: String,
        status: Int,
    ): RepositoryError.Api {
        return RepositoryError.Api(
            code = code.code,
            message = message,
            status = status,
        )
    }

    /*
     * Bewust integers in plaats van Ktor HttpStatusCode.
     * Zo blijft de repositorylaag onafhankelijk van Ktor.
     */
    private const val HTTP_BAD_REQUEST = 400
    private const val HTTP_FORBIDDEN = 403
    private const val HTTP_NOT_FOUND = 404
    private const val HTTP_CONFLICT = 409
}