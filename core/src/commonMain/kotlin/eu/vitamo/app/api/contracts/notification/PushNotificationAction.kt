package eu.vitamo.app.api.contracts.notification

import kotlin.uuid.Uuid

enum class PushNotificationActionType(
    val wireValue: String,
) {
    OPEN_FRIEND_REQUESTS(
        wireValue = "open_friend_requests",
    );

    companion object {
        fun fromWireValue(
            value: String?,
        ): PushNotificationActionType? {
            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { type ->
                type.wireValue.equals(
                    other = value,
                    ignoreCase = true,
                )
            }
        }
    }
}

sealed interface PushNotificationAction {

    val type: PushNotificationActionType

    val targetId: Uuid?

    data class OpenFriendRequests(
        val userId: Uuid? = null,
    ) : PushNotificationAction {

        override val type:
                PushNotificationActionType =
            PushNotificationActionType
                .OPEN_FRIEND_REQUESTS

        override val targetId: Uuid?
            get() = userId
    }

    companion object {
        fun from(
            type: String?,
            targetId: String?,
        ): PushNotificationAction? {
            val parsedType =
                PushNotificationActionType
                    .fromWireValue(
                        value = type,
                    )
                    ?: return null

            val parsedTargetId =
                targetId
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let { value ->
                        runCatching {
                            Uuid.parse(value)
                        }.getOrNull()
                    }

            return when (parsedType) {
                PushNotificationActionType
                    .OPEN_FRIEND_REQUESTS -> {
                    OpenFriendRequests(
                        userId = parsedTargetId,
                    )
                }
            }
        }
    }
}