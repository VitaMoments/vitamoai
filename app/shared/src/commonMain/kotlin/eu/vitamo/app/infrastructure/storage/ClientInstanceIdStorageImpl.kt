package eu.vitamo.app.infrastructure.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

class ClientInstanceIdStorageImpl(
    private val settings: Settings,
) : ClientInstanceIdStorage {

    private val mutex = Mutex()

    override suspend fun getOrCreate(): Uuid {
        return mutex.withLock {
            val existing = settings
                .getStringOrNull(
                    key = KEY_CLIENT_INSTANCE_ID,
                )
                ?.let { value ->
                    runCatching {
                        Uuid.parse(value)
                    }.getOrNull()
                }

            if (existing != null) {
                return@withLock existing
            }

            val newId = Uuid.random()

            settings.putString(
                key = KEY_CLIENT_INSTANCE_ID,
                value = newId.toString(),
            )

            newId
        }
    }

    private companion object {
        const val KEY_CLIENT_INSTANCE_ID =
            "device.client_instance_id"
    }
}