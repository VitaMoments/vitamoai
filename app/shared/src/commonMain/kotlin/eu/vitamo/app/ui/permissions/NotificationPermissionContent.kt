package eu.vitamo.app.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.vitamo.app.infrastructure.permissions.notification.NotificationPermissionEvent
import eu.vitamo.app.infrastructure.permissions.notification.NotificationPermissionState

@Composable
fun NotificationPermissionContent(
    state: NotificationPermissionState,
    onEvent: (NotificationPermissionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Notificaties",
            style = MaterialTheme.typography.headlineSmall,
        )

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.isGranted -> {
                Text(
                    text = "Notificaties zijn ingeschakeld.",
                )
            }

            state.canRequest -> {
                Text(
                    text = "Schakel notificaties in om meldingen te ontvangen.",
                )

                Button(
                    onClick = {
                        onEvent(
                            NotificationPermissionEvent.RequestPermission,
                        )
                    },
                ) {
                    Text(
                        text = "Notificaties inschakelen",
                    )
                }
            }

            state.requiresSettings -> {
                Text(
                    text = "Notificaties zijn uitgeschakeld. Je kunt ze inschakelen via de instellingen van je telefoon.",
                )

                Button(
                    onClick = {
                        onEvent(
                            NotificationPermissionEvent.OpenSettings,
                        )
                    },
                ) {
                    Text(
                        text = "Open instellingen",
                    )
                }
            }
        }
    }
}