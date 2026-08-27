package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable

@Serializable
enum class FeedItemPermission {
    GRANTED,
    DENIED
}