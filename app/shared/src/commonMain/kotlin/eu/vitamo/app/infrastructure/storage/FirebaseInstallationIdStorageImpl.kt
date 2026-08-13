package eu.vitamo.app.infrastructure.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirebaseInstallationIdStorageImpl(
    private val settings: Settings,
) : FirebaseInstallationIdStorage {

    private val mutex = Mutex()

    override suspend fun get(): String? {
        return mutex.withLock {
            settings.getStringOrNull(
                key = KEY_FIREBASE_INSTALLATION_ID,
            )
        }
    }

    override suspend fun set(
        installationId: String,
    ) {
        mutex.withLock {
            settings.putString(
                key = KEY_FIREBASE_INSTALLATION_ID,
                value = installationId,
            )
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            settings.remove(
                key = KEY_FIREBASE_INSTALLATION_ID,
            )
        }
    }

    private companion object {
        const val KEY_FIREBASE_INSTALLATION_ID =
            "device.firebase_installation_id"
    }
}