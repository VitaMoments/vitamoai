package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.subscribtions.SubscriptionPlan
import eu.vitamo.app.api.contracts.user.capabilities.UserAction
import eu.vitamo.app.api.contracts.user.capabilities.UserCapabilities
import eu.vitamo.app.api.contracts.user.capabilities.UserLimits
import eu.vitamo.app.features.user.repository.UserSubscriptionRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid

class UserCapabilitiesProviderImpl(
    private val subscriptionRepository: UserSubscriptionRepository,
) : UserCapabilitiesProvider {

    override suspend fun resolve(
        userId: Uuid,
    ): RepositoryResult<UserCapabilities> =
        when (
            val result = subscriptionRepository.resolvePlan(
                userId = userId,
                now = Clock.System.now(),
            )
        ) {
            is RepositoryResult.Success -> {
                RepositoryResult.Success(
                    data = capabilitiesFor(
                        plan = result.data,
                    ),
                )
            }

            is RepositoryResult.Error -> {
                result
            }
        }

    private fun capabilitiesFor(
        plan: SubscriptionPlan,
    ): UserCapabilities =
        when (plan) {
            SubscriptionPlan.BASIC ->
                basicCapabilities()

            SubscriptionPlan.PLUS ->
                plusCapabilities()
        }

    override fun basicCapabilities(): UserCapabilities =
        UserCapabilities(
            actions = setOf(
                UserAction.POST_CREATE,
                UserAction.POST_EDIT,
                UserAction.POST_DELETE,
                UserAction.POST_LIKE,
            ),
            limits = basicLimits(),
        )

    private fun plusCapabilities(): UserCapabilities =
        UserCapabilities(
            actions = setOf(
                UserAction.POST_CREATE,
                UserAction.POST_EDIT,
                UserAction.POST_DELETE,
                UserAction.POST_LIKE,

                UserAction.COMMENT_CREATE,
                UserAction.COMMENT_EDIT,
                UserAction.COMMENT_DELETE,
                UserAction.COMMENT_REPLY,
                UserAction.COMMENT_LIKE,
            ),
            limits = plusLimits(),
        )

    private fun basicLimits(): UserLimits =
        UserLimits(
            maxImagesPerPost = BASIC_MAX_IMAGES_PER_POST,
            maxPostLength = BASIC_MAX_POST_LENGTH,
        )

    private fun plusLimits(): UserLimits =
        UserLimits(
            maxImagesPerPost = PLUS_MAX_IMAGES_PER_POST,
            maxPostLength = PLUS_MAX_POST_LENGTH,
        )

    private companion object {
        // Zet hier je definitieve productwaardes.
        const val BASIC_MAX_IMAGES_PER_POST = 5
        const val BASIC_MAX_POST_LENGTH = 500

        const val PLUS_MAX_IMAGES_PER_POST = 5
        const val PLUS_MAX_POST_LENGTH = 500
    }
}