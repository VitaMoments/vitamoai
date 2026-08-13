package eu.vitamo.app.infrastructure.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import eu.vitamo.app.api.contracts.notification.PushNotificationDataKeys
import eu.vitamo.app.app.shared.R

class AndroidLocalNotificationManager(
    private val context: Context,
) : PlatformLocalNotificationManager {

    @SuppressLint("MissingPermission")
    override suspend fun show(
        notification: AppNotification,
    ) {
        val channelConfig =
            notification.channel.toAndroidChannelConfig()

        createNotificationChannel(
            config = channelConfig,
        )

        val builder =
            NotificationCompat.Builder(
                context,
                channelConfig.id,
            )
                .setSmallIcon(
                    R.drawable.vitamo_icon_front,
                )
                .setContentTitle(
                    notification.title,
                )
                .setContentText(
                    notification.body,
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            notification.body,
                        ),
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT,
                )
                .setAutoCancel(true)

        createContentIntent(
            notification = notification,
        )?.let { contentIntent ->
            builder.setContentIntent(
                contentIntent,
            )
        }

        val androidNotification =
            builder.build()

        NotificationManagerCompat
            .from(context)
            .notify(
                notification.id,
                NOTIFICATION_ID,
                androidNotification,
            )
    }

    override suspend fun cancel(
        id: String,
    ) {
        NotificationManagerCompat
            .from(context)
            .cancel(
                id,
                NOTIFICATION_ID,
            )
    }

    private fun createContentIntent(
        notification: AppNotification,
    ): PendingIntent? {
        val action =
            notification.action
                ?: return null

        val launchIntent =
            context.packageManager
                .getLaunchIntentForPackage(
                    context.packageName,
                )
                ?: return null

        launchIntent.apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(
                PushNotificationDataKeys.ACTION,
                action.type.wireValue,
            )

            action.targetId?.let { targetId ->
                putExtra(
                    PushNotificationDataKeys.TARGET_ID,
                    targetId.toString(),
                )
            }
        }

        return PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel(
        config: AndroidNotificationChannelConfig,
    ) {
        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE,
            ) as NotificationManager

        val channel =
            NotificationChannel(
                config.id,
                config.name,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description =
                    config.description
            }

        notificationManager
            .createNotificationChannel(
                channel,
            )
    }

    private companion object {
        const val NOTIFICATION_ID =
            0
    }
}