package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface UserRepository {

    /**
     * Haalt het volledige profiel van de ingelogde gebruiker op.
     *
     * Serverendpoint:
     * GET /users/me
     */
    suspend fun getCurrentUser():
            RepositoryResult<UserWithContext>

    /**
     * Haalt het profiel van een specifieke gebruiker op.
     *
     * Serverendpoint:
     * GET /users/{userId}
     */
    suspend fun getUser(
        userId: Uuid,
    ): RepositoryResult<UserWithContext>

    /**
     * Zoekt naar gebruikers.
     *
     * Serverendpoint:
     * GET /users/search
     */
    suspend fun searchUsers(
        query: String?,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Long = 0L,
    ): RepositoryResult<PagedResult<UserWithContext>>

    /**
     * Uploadt een afbeelding en stelt deze direct in als profielfoto
     * van de ingelogde gebruiker.
     *
     * De server:
     * - bepaalt de huidige gebruiker vanuit de sessie;
     * - verwerkt en valideert de afbeelding;
     * - maakt de MediaAsset aan;
     * - slaat de afbeelding op;
     * - koppelt de MediaAsset aan de gebruiker;
     * - retourneert de bijgewerkte UserWithContext.
     *
     * Serverendpoint:
     * PUT /users/me/profile-image
     */
    suspend fun updateProfileImage(
        image: PickedImage,
    ): RepositoryResult<UserWithContext>

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 20
    }
}