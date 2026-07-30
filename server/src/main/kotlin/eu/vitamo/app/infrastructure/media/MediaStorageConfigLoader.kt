package eu.vitamo.app.infrastructure.media

import eu.vitamo.app.config.EnvLoader
import eu.vitamo.app.config.EnvLoader.readRequired
import java.nio.file.Path
import java.util.Properties

object MediaStorageConfigLoader {
    private const val MEDIA_STORAGE_ROOT = "MEDIA_STORAGE_ROOT"
    fun loadOrThrow(
        environment: Map<String, String> = System.getenv(),
        systemProperties: Properties = System.getProperties(),
    ): MediaStorageConfig {
        val mediaStorageRoot = readRequired(
            MEDIA_STORAGE_ROOT,
            environment,
            systemProperties
        )

        return MediaStorageConfig(
            rootDirectory = Path.of(mediaStorageRoot),
        )
    }
}