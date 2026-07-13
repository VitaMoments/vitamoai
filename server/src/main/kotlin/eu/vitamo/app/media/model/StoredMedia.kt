package eu.vitamo.app.media.model

import kotlinx.io.Source

data class StoredMedia(
    val source: Source,
    val contentType: String,
    val contentLength: Long?
)