package eu.vitamo.app.api.contracts.media

import kotlinx.serialization.Serializable

@Serializable
enum class MediaPurposeType {
    PROFILE,
    POST,
    FEED,
    COVER
}