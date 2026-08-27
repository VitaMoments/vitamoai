package eu.vitamo.app.features.feed.model

class FeedMediaUpload(
    val bytes: ByteArray,
    val fileName: String? = null,
    val mimeType: String? = null,
)