package eu.vitamo.app.infrastructure.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

        val androidNotification =
            NotificationCompat.Builder(
                context,
                channelConfig.id,
            )
                .setSmallIcon(
                    R.drawable.vitamo_icon_front,
                )
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notification.body),
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH,
                )
                .setAutoCancel(true)
                .build()

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

    private fun createNotificationChannel(
        config: AndroidNotificationChannelConfig,
    ) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE,
            ) as NotificationManager

        val channel = NotificationChannel(
            config.id,
            config.name,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = config.description
        }

        notificationManager.createNotificationChannel(
            channel,
        )
    }

    private companion object {
        const val NOTIFICATION_ID = 0
    }
}