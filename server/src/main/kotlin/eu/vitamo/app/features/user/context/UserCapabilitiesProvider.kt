package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.user.capabilities.UserCapabilities
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface UserCapabilitiesProvider {

    suspend fun resolve(
        userId: Uuid,
    ): RepositoryResult<UserCapabilities>

    fun basicCapabilities(): UserCapabilities
}