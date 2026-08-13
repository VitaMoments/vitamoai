package eu.vitamo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eu.vitamo.app.api.contracts.notification.PushNotificationAction
import eu.vitamo.app.api.contracts.notification.PushNotificationDataKeys
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val notificationAction = MutableStateFlow<PushNotificationAction?>(null)

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val initialDeepLink = intent?.dataString

        initialDeepLink?.let { link ->
            Log.d(
                "MainActivity",
                "Initial deep link: $link",
            )
        }

        handleNotificationIntent(intent = intent,)

        setContent {
            val currentNotificationAction by notificationAction.collectAsState()

            App(
                initialDeepLink = initialDeepLink,
                notificationAction = currentNotificationAction,
                onNotificationActionConsumed = {
                    notificationAction.value =
                        null

                    intent?.removeExtra(
                        PushNotificationDataKeys.ACTION,
                    )

                    intent?.removeExtra(
                        PushNotificationDataKeys.TARGET_ID,
                    )
                },
            )
        }
    }

    override fun onNewIntent(
        intent: Intent,
    ) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent = intent)
    }

    private fun handleNotificationIntent(
        intent: Intent?,
    ) {
        val action = PushNotificationAction.from(
            type = intent?.getStringExtra(PushNotificationDataKeys.ACTION),
            targetId = intent?.getStringExtra(PushNotificationDataKeys.TARGET_ID),
        ) ?: return
        notificationAction.value = action
    }
}