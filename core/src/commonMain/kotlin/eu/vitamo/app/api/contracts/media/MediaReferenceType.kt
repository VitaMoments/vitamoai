package eu.vitamo.app.api.contracts.media

import kotlinx.serialization.Serializable

@Serializable
enum class MediaReferenceType {
    USER,
    FEED_ITEM,
    POST,
    COMMENT
}