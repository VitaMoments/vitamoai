package eu.vitamo.app.api.contracts.user.capabilities

import kotlinx.serialization.Serializable

@Serializable
enum class UserAction {
    POST_CREATE,
    POST_EDIT,
    POST_DELETE,
    POST_LIKE,

    COMMENT_CREATE,
    COMMENT_EDIT,
    COMMENT_DELETE,
    COMMENT_REPLY,
    COMMENT_LIKE
}