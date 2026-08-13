package eu.vitamo.app.ui.user.friends

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.uuid.Uuid

@Composable
fun FriendRequestsScreen(initialUserId: Uuid?) {
    Text("You got a request from $initialUserId")
}