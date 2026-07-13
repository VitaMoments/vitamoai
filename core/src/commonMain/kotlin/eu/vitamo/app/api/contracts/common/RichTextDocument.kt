package eu.vitamo.app.api.contracts.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RichTextDocument(
    val type: String? = null,
    val content: JsonElement? = null
)
