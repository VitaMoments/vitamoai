package eu.vitamo.app.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.media.MediaAssetResponse
import eu.vitamo.app.api.contracts.media.MediaPurposeType
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

data class UploadedMediaAsset(
    val uuid: Uuid,
    val url: String,
    val contentType: String,
    val sizeBytes: Long,
    val purpose: MediaPurposeType,
    val privacy: PrivacyStatus,
    val referenceType: MediaReferenceType,
    val referenceId: Uuid,
)

class MediaUploadService(
    private val httpClient: HttpClient,
) {
    suspend fun uploadMediaAsset(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType,
        privacy: PrivacyStatus = PrivacyStatus.FRIENDS_ONLY,
    ): RepositoryResult<UploadedMediaAsset> = runCatching {
        val normalizedContentType = normalizeUploadContentType(contentType, fileName)
        val safeFileName = sanitizeFileName(fileName, normalizedContentType)
        val formData = formData {
            append("file", fileBytes, Headers.build {
                append(HttpHeaders.ContentType, normalizedContentType)
                append(HttpHeaders.ContentDisposition, "filename=\"$safeFileName\"")
            })
            append("referenceId", referenceId.toString())
            append("referenceType", referenceType.name)
            append("purpose", purpose.name)
            append("privacy", privacy.name)
        }

        val response: MediaAssetResponse = httpClient.post("/api/media") {
            setBody(MultiPartFormDataContent(formData))
        }.body()

        RepositoryResult.Success(
            UploadedMediaAsset(
                uuid = response.uuid,
                url = response.url,
                contentType = response.contentType,
                sizeBytes = response.sizeBytes,
                purpose = response.purpose,
                privacy = response.privacy,
                referenceType = response.referenceType,
                referenceId = response.referenceId,
            )
        )
    }.getOrElse { error ->
        RepositoryResult.Error(
            RepositoryError.Unknown(
                message = "Media upload failed: ${error.message ?: "unknown error"}"
            )
        )
    }

    suspend fun uploadMultipleMediaAssets(
        files: List<Pair<ByteArray, String>>, // List of (bytes, fileName) pairs
        contentType: String,
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        purpose: MediaPurposeType,
        privacy: PrivacyStatus = PrivacyStatus.FRIENDS_ONLY,
    ): RepositoryResult<List<UploadedMediaAsset>> {
        val results = mutableListOf<UploadedMediaAsset>()
        for ((bytes, fileName) in files) {
            when (val result = uploadMediaAsset(
                fileBytes = bytes,
                fileName = fileName,
                contentType = contentType,
                referenceId = referenceId,
                referenceType = referenceType,
                purpose = purpose,
                privacy = privacy,
            )) {
                is RepositoryResult.Success -> results.add(result.data)
                is RepositoryResult.Error -> return result
            }
        }
        return RepositoryResult.Success(results)
    }

    private fun normalizeUploadContentType(raw: String, fileName: String): String {
        val value = raw.trim().lowercase()
        if (value.isBlank()) {
            return guessContentTypeFromFileName(fileName)
        }

        return when (value) {
            "image/jpg", "image/pjpeg" -> "image/jpeg"
            "image/x-png" -> "image/png"
            "image/heif-sequence" -> "image/heif"
            "image/heic-sequence" -> "image/heic"
            "application/octet-stream" -> guessContentTypeFromFileName(fileName)
            else -> value
        }
    }

    private fun guessContentTypeFromFileName(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "jpe", "jfif" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            else -> "application/octet-stream"
        }
    }

    private fun sanitizeFileName(rawFileName: String, normalizedContentType: String): String {
        val trimmed = rawFileName.trim().ifBlank { "upload" }
        val withoutControlChars = trimmed.replace(Regex("[\\r\\n\\t]"), "_")
        val safeAscii = buildString(withoutControlChars.length) {
            withoutControlChars.forEach { c ->
                if (c.isLetterOrDigit() || c == '.' || c == '_' || c == '-') {
                    append(c)
                } else {
                    append('_')
                }
            }
        }.trim('_').ifBlank { "upload" }

        if (safeAscii.contains('.')) {
            return safeAscii
        }

        val extension = when (normalizedContentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            else -> "bin"
        }
        return "$safeAscii.$extension"
    }
}


