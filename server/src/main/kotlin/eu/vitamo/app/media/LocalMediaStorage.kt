package eu.vitamo.app.media

import eu.vitamo.app.media.model.StoredMedia
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class LocalMediaStorage(
    private val baseDir: String
) : MediaStorage {

    override suspend fun save(
        objectKey: String,
        bytes: ByteArray,
        contentType: String
    ) {
        val filePath = resolvePath(objectKey)
        val parentPath = filePath.parent
            ?: error("Missing parent path for objectKey=$objectKey")

        SystemFileSystem.createDirectories(parentPath)
        SystemFileSystem.sink(filePath).buffered().use { sink ->
            sink.write(bytes)
        }
    }

    override suspend fun read(objectKey: String): StoredMedia {
        val filePath = resolvePath(objectKey)

        require(SystemFileSystem.exists(filePath)) {
            "Media file not found for objectKey=$objectKey"
        }

        val metadata = SystemFileSystem.metadataOrNull(filePath)
            ?: error("Could not read metadata for objectKey=$objectKey")

        return StoredMedia(
            source = SystemFileSystem.source(filePath).buffered(),
            contentType = guessContentType(filePath.name),
            contentLength = metadata.size
        )
    }

    override suspend fun delete(objectKey: String) {
        val filePath = resolvePath(objectKey)
        if (SystemFileSystem.exists(filePath)) {
            SystemFileSystem.delete(filePath, mustExist = false)
        }
    }

    override fun buildObjectKey(
        referenceType: String,
        referenceId: String,
        mediaId: String,
        fileExtension: String
    ): String {
        return "${referenceType.lowercase()}/$referenceId/$mediaId/original.$fileExtension"
    }

    private fun resolvePath(objectKey: String): Path {
        val safePath = objectKey
            .removePrefix("/")
            .replace("..", "")

        return Path("$baseDir/$safePath")
    }

    private fun guessContentType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".webp", true) -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}