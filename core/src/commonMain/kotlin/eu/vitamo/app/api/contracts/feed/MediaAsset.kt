package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

@Serializable
data class MediaAsset(
    @Contextual
    val id: Uuid,
    val url: String,
    val type: MediaAssetType,
    val metadata: JsonElement? = null,
)

@Serializable
enum class MediaAssetType {
    IMAGE,
    VIDEO,
    DOCUMENT,
}
