package eu.vitamo.app.features.media.model

import eu.vitamo.app.features.media.storage.MediaReadHandle

data class MediaContent(
    val asset: MediaAssetRecord,
    val readHandle: MediaReadHandle,
)