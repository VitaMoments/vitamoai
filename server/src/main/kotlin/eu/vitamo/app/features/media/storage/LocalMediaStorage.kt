package eu.vitamo.app.features.media.storage

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaStorage(
    rootDirectory: Path,
) : MediaStorage {

    private val rootDirectory = rootDirectory
        .toAbsolutePath()
        .normalize()

    init {
        Files.createDirectories(this.rootDirectory)
    }

    override suspend fun store(
        storageKey: String,
        source: Path,
    ): StoredMediaObject = withContext(Dispatchers.IO) {
        require(Files.isRegularFile(source)) {
            "Media source is not a regular file."
        }

        val target = resolveSafely(storageKey)

        Files.createDirectories(
            target.parent,
        )

        val temporaryTarget = target.resolveSibling(
            "${target.fileName}.uploading-${UUID.randomUUID()}",
        )

        try {
            Files.copy(
                source,
                temporaryTarget,
                StandardCopyOption.REPLACE_EXISTING,
            )

            moveAtomically(
                source = temporaryTarget,
                target = target,
            )
        } finally {
            Files.deleteIfExists(temporaryTarget)
        }

        StoredMediaObject(
            storageKey = storageKey,
            sizeBytes = Files.size(target),
        )
    }

    override suspend fun open(
        storageKey: String,
    ): MediaReadHandle? = withContext(Dispatchers.IO) {
        val path = resolveSafely(storageKey)

        if (!Files.isRegularFile(path)) {
            return@withContext null
        }

        MediaReadHandle.LocalFile(
            path = path,
            sizeBytes = Files.size(path),
        )
    }

    override suspend fun delete(
        storageKey: String,
    ) {
        withContext(Dispatchers.IO) {
            Files.deleteIfExists(
                resolveSafely(storageKey),
            )
        }
    }

    private fun resolveSafely(
        storageKey: String,
    ): Path {
        require(storageKey.isNotBlank()) {
            "Storage key cannot be blank."
        }

        val relativePath = Path.of(storageKey)

        require(!relativePath.isAbsolute) {
            "Absolute storage paths are not allowed."
        }

        val resolvedPath = rootDirectory
            .resolve(relativePath)
            .normalize()

        require(resolvedPath.startsWith(rootDirectory)) {
            "Storage path escapes the configured media directory."
        }

        return resolvedPath
    }

    private fun moveAtomically(
        source: Path,
        target: Path,
    ) {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}