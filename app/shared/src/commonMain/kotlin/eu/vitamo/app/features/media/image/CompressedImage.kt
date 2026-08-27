package eu.vitamo.app.features.media.image

data class CompressedImage(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)