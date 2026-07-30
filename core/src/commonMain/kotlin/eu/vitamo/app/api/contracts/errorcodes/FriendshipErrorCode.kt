package eu.vitamo.app.api.contracts.errorcodes

import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.api.result.ErrorCodeDefinition

enum class FriendshipErrorCode(
    override val code: ErrorCode,
) : ErrorCodeDefinition {

    CANNOT_FRIEND_SELF(
        code = ErrorCode(
            "CANNOT_FRIEND_SELF",
        ),
    ),

    FRIEND_REQUEST_ALREADY_EXISTS(
        code = ErrorCode(
            "FRIEND_REQUEST_ALREADY_EXISTS",
        ),
    ),

    INCOMING_FRIEND_REQUEST_EXISTS(
        code = ErrorCode(
            "INCOMING_FRIEND_REQUEST_EXISTS",
        ),
    ),

    FRIEND_REQUEST_NOT_FOUND(
        code = ErrorCode(
            "FRIEND_REQUEST_NOT_FOUND",
        ),
    ),

    FRIEND_REQUEST_NOT_PENDING(
        code = ErrorCode(
            "FRIEND_REQUEST_NOT_PENDING",
        ),
    ),

    NOT_FRIEND_REQUEST_RECIPIENT(
        code = ErrorCode(
            "NOT_FRIEND_REQUEST_RECIPIENT",
        ),
    ),

    NOT_FRIENDSHIP_PARTICIPANT(
        code = ErrorCode(
            "NOT_FRIENDSHIP_PARTICIPANT",
        ),
    ),

    ALREADY_FRIENDS(
        code = ErrorCode(
            "ALREADY_FRIENDS",
        ),
    ),

    FRIENDSHIP_NOT_FOUND(
        code = ErrorCode(
            "FRIENDSHIP_NOT_FOUND",
        ),
    ),
}