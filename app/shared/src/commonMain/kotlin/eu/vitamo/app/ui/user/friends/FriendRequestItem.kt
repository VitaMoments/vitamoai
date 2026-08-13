package eu.vitamo.app.ui.user.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.ui.user.components.UserAvatar
import kotlin.uuid.Uuid

@Composable
fun FriendRequestItem(
    user: UserWithContext,
    profileImageUrl: String?,
    onAccept: (userId: Uuid) -> Unit,
    onReject: (userId: Uuid) -> Unit,
    onRevoke: (userId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    user = user.user,
                    profileImageUrl = profileImageUrl,
                    showLoadingIndicator = false,
                    modifier = Modifier.size(52.dp),
                )

                Spacer(
                    modifier = Modifier.width(12.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = user.user.displayName,
                        style =
                            MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                    )

                    val subtitle =
                        when (
                            user.friendshipContext.state
                        ) {
                            FriendshipState
                                .INCOMING_REQUEST ->
                                "Wil vrienden met je worden"

                            FriendshipState
                                .OUTGOING_REQUEST ->
                                "Vriendschapsverzoek verzonden"

                            else ->
                                null
                        }

                    subtitle?.let { text ->
                        Text(
                            text = text,
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            when (
                user.friendshipContext.state
            ) {
                FriendshipState.INCOMING_REQUEST -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                onReject(
                                    user.user.id,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Weigeren",
                            )
                        }

                        Button(
                            onClick = {
                                onAccept(
                                    user.user.id,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Accepteren",
                            )
                        }
                    }
                }

                FriendshipState.OUTGOING_REQUEST -> {
                    OutlinedButton(
                        onClick = {
                            onRevoke(
                                user.user.id,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Verzoek intrekken",
                        )
                    }
                }

                FriendshipState.NONE, FriendshipState.FRIENDS -> Unit
            }
        }
    }
}