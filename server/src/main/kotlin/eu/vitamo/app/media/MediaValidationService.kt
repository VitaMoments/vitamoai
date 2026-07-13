package eu.vitamo.app.media

class MediaValidationService(
    private val maxBytes: Int = 100 * 1024 * 1024
) {
    private val allowedContentTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif"
    )

    fun canonicalContentType(contentType: String?): String? = normalizeContentType(contentType)

    fun validateImage(
        contentType: String?,
        bytes: ByteArray
    ): Result<Unit> {
        val normalizedContentType = normalizeContentType(contentType) ?: return Result.failure(
            IllegalArgumentException("Missing content type")
        )
        if (normalizedContentType !in allowedContentTypes) {
            return Result.failure(IllegalArgumentException("Unsupported content type: $normalizedContentType"))
        }
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Uploaded file is empty"))
        }
        if (bytes.size > maxBytes) {
            return Result.failure(IllegalArgumentException("File too large. Max is $maxBytes bytes"))
        }

        return Result.success(Unit)
    }

    fun extensionFor(contentType: String): String {
        return when (normalizeContentType(contentType) ?: contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            else -> error("Unsupported content type: $contentType")
        }
    }

    private fun normalizeContentType(raw: String?): String? {
        val value = raw?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        if (value.isBlank()) return null
        return when (value) {
            "image/jpg", "image/pjpeg" -> "image/jpeg"
            "image/x-png" -> "image/png"
            "image/heic-sequence" -> "image/heic"
            "image/heif-sequence" -> "image/heif"
            else -> value
        }
    }
}