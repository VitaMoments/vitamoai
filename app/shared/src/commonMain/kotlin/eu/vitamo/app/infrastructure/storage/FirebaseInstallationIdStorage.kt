package eu.vitamo.app.infrastructure.storage

interface FirebaseInstallationIdStorage {

    suspend fun get(): String?

    suspend fun set(
        installationId: String,
    )

    suspend fun clear()
}