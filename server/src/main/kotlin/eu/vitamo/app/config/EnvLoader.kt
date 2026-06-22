package eu.vitamo.app.config

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile

object EnvLoader {
    private val dotEnvPath: Path? by lazy {
        findDotEnvPath()
    }

    private val dotEnvValues: Map<String, String> by lazy {
        val path = dotEnvPath

        if (path != null) {
            println("Loading .env from: ${path.absolutePathString()}")
            loadDotEnv(path)
        } else {
            println("No .env file found. Using environment and system properties only.")
            emptyMap()
        }
    }

    fun read(
        key: String,
        environment: Map<String, String> = System.getenv(),
        systemProperties: Properties = System.getProperties(),
    ): String? {
        return environment[key]
            ?.takeIf { it.isNotBlank() }
            ?: systemProperties.getProperty(key)
                ?.takeIf { it.isNotBlank() }
            ?: dotEnvValues[key]
                ?.takeIf { it.isNotBlank() }
    }

    private fun findDotEnvPath(): Path? {
        var current: Path? = Path.of(System.getProperty("user.dir")).toAbsolutePath()

        while (current != null) {
            val candidate = current.resolve(".env")

            if (Files.exists(candidate) && candidate.isRegularFile()) {
                return candidate
            }

            current = current.parent
        }

        return null
    }

    private fun loadDotEnv(dotEnvPath: Path): Map<String, String> {
        return Files.readAllLines(dotEnvPath)
            .asSequence()
            .map(String::trim)
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            .filter { it.contains("=") }
            .map { line ->
                val separatorIndex = line.indexOf('=')
                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1)
                    .trim()
                    .trim('"', '\'')

                key to value
            }
            .filter { (key, _) -> key.isNotBlank() }
            .toMap()
    }
}