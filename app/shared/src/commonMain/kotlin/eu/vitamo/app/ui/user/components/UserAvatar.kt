package eu.vitamo.app.ui.user.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.ui.media.component.rememberAppImageLoader

@Composable
fun UserAvatar(
    user: User,
    profileImageUrl: String?,
    showLoadingIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageLoader = rememberAppImageLoader()

    Box(
        modifier = modifier.size(112.dp),
        contentAlignment = Alignment.Center,
    ) {
        ProfileInitialsAvatar(
            user = user,
            modifier = Modifier.fillMaxSize(),
        )

        if (profileImageUrl != null) {
            AsyncImage(
                model = profileImageUrl,
                imageLoader = imageLoader,
                contentDescription =
                    "Profielfoto van ${user.displayName}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }

        if (showLoadingIndicator) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme
                    .colorScheme
                    .scrim
                    .copy(alpha = 0.45f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme
                            .colorScheme
                            .onPrimary,
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInitialsAvatar(
    user: User,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.initials(),
                style = MaterialTheme.typography.headlineLarge,
                color =
                    MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun User.initials(): String {
    return displayName
        .trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { namePart ->
            namePart
                .firstOrNull()
                ?.uppercase()
        }
        .joinToString(separator = "")
        .ifBlank {
            "?"
        }
}