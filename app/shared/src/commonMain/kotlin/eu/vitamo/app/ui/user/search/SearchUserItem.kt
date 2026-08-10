package eu.vitamo.app.ui.user.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.ui.user.components.UserAvatar
import kotlin.uuid.Uuid

@Composable
fun SearchUserItem(
    user: UserWithContext,
    profileImageUrl: String?,
    onClick: (userId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(user.user.id) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            user = user.user,
            profileImageUrl = profileImageUrl,
            showLoadingIndicator = false,
            modifier = Modifier.size(48.dp),
        )
        Text(
            modifier = Modifier.weight(1f)
                .padding(horizontal = 8.dp),
            text = user.user.displayName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}