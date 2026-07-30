package eu.vitamo.app.infrastructure.media

import java.nio.file.Path

data class MediaStorageConfig(
    val rootDirectory: Path,
)