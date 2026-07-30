package eu.vitamo.app.features.media.storage

import java.nio.file.Path

data class StoredMediaObject(
    val storageKey: String,
    val sizeBytes: Long,
)

sealed interface MediaReadHandle {

    data class LocalFile(
        val path: Path,
        val sizeBytes: Long,
    ) : MediaReadHandle

    data class Redirect(
        val url: String,
    ) : MediaReadHandle
}

interface MediaStorage {

    suspend fun store(
        storageKey: String,
        source: Path,
    ): StoredMediaObject

    suspend fun open(
        storageKey: String,
    ): MediaReadHandle?

    suspend fun delete(
        storageKey: String,
    )
}