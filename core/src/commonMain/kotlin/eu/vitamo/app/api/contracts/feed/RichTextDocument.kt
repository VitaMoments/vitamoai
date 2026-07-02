package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RichTextDocument(
    val type: String,
    val content: JsonElement,
)
