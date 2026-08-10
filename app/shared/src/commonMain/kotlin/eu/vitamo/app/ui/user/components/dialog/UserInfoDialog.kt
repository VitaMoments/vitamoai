package eu.vitamo.app.ui.user.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.ui.user.components.UserAvatar

@Composable
fun UserInfoDialog(
    userContext: UserWithContext,
    profileImageUrl: String?,
    isFriendshipActionLoading: Boolean = false,
    onSendFriendRequest: () -> Unit,
    onCancelFriendRequest: () -> Unit,
    onAcceptFriendRequest: () -> Unit,
    onRejectFriendRequest: () -> Unit,
    onRemoveFriendship: () -> Unit,
    onDismiss: () -> Unit,
) {
    val user = userContext.user
    val friendshipContext = userContext.friendshipContext

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UserAvatar(
                    user = user,
                    profileImageUrl = profileImageUrl,
                    showLoadingIndicator = false,
                    modifier = Modifier.size(112.dp),
                )

                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                user.displayName
                    .takeIf(String::isNotBlank)
                    ?.let { alias ->
                        Text(
                            text = "@$alias",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                user.bio
                    ?.takeIf(String::isNotBlank)
                    ?.let { bio ->
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                when (friendshipContext.state) {
                    FriendshipState.NONE -> {
                        Button(
                            onClick = onSendFriendRequest,
                            enabled = !isFriendshipActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            FriendshipButtonContent(
                                isLoading = isFriendshipActionLoading,
                                text = "Vriend toevoegen",
                            )
                        }
                    }

                    FriendshipState.OUTGOING_REQUEST -> {
                        Text(
                            text = "Vriendschapsverzoek verzonden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedButton(
                            onClick = onCancelFriendRequest,
                            enabled = !isFriendshipActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            FriendshipButtonContent(
                                isLoading = isFriendshipActionLoading,
                                text = "Verzoek annuleren",
                            )
                        }
                    }

                    FriendshipState.INCOMING_REQUEST -> {
                        Text(
                            text = "Deze gebruiker wil vrienden worden",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )

                        Button(
                            onClick = onAcceptFriendRequest,
                            enabled = !isFriendshipActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            FriendshipButtonContent(
                                isLoading = isFriendshipActionLoading,
                                text = "Accepteren",
                            )
                        }

                        OutlinedButton(
                            onClick = onRejectFriendRequest,
                            enabled = !isFriendshipActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Afwijzen")
                        }
                    }

                    FriendshipState.FRIENDS -> {
                        Text(
                            text = "Jullie zijn vrienden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        OutlinedButton(
                            onClick = onRemoveFriendship,
                            enabled = !isFriendshipActionLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            FriendshipButtonContent(
                                isLoading = isFriendshipActionLoading,
                                text = "Vriendschap verwijderen",
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !isFriendshipActionLoading,
                ) {
                    Text("Sluiten")
                }
            }
        }
    }
}

@Composable
private fun FriendshipButtonContent(
    isLoading: Boolean,
    text: String,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
        )
    } else {
        Text(text)
    }
}