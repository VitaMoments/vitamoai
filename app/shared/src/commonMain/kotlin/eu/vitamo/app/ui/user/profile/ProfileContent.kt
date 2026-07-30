package eu.vitamo.app.ui.user.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.PublicUser
import eu.vitamo.app.api.contracts.user.User
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    state: ProfileState,
    profileImageUrl: String?,
    imageLoader: ImageLoader,
    onRetryClicked: () -> Unit,
    onChangeProfileImageClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column( modifier = modifier
        .fillMaxSize()
        .safeContentPadding()) {
        when {
            state.isLoading && state.profile == null -> {
                ProfileLoadingContent(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            state.profile != null -> {
                LoadedProfileContent(
                    user = state.profile.user,
                    profileImageUrl = profileImageUrl,
                    imageLoader = imageLoader,
                    isProfileImageUploading =
                        state.isProfileImageUploading,
                    onChangeProfileImageClicked =
                        onChangeProfileImageClicked,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                ProfileErrorContent(
                    message = state.errorMessage
                        ?: "Het profiel kon niet worden geladen.",
                    onRetryClicked = onRetryClicked,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (state.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}


@Composable
private fun LoadedProfileContent(
    user: User,
    profileImageUrl: String?,
    imageLoader: ImageLoader,
    isProfileImageUploading: Boolean,
    onChangeProfileImageClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
            )
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileHeader(
            user = user,
            profileImageUrl = profileImageUrl,
            imageLoader = imageLoader,
            isProfileImageUploading =
                isProfileImageUploading,
            onChangeProfileImageClicked =
                onChangeProfileImageClicked,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        ProfileInformationCard(
            user = user,
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    profileImageUrl: String?,
    imageLoader: ImageLoader,
    isProfileImageUploading: Boolean,
    onChangeProfileImageClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(
            user = user,
            profileImageUrl = profileImageUrl,
            imageLoader = imageLoader,
            isUploading = isProfileImageUploading,
        )

        if (user is AuthenticatedUser) {
            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Button(
                onClick = onChangeProfileImageClicked,
                enabled = !isProfileImageUploading,
            ) {
                if (isProfileImageUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp),
                    )
                }

                Text(
                    text = if (isProfileImageUploading) {
                        "Profielfoto uploaden…"
                    } else {
                        "Profielfoto wijzigen"
                    },
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        val alias = (user as? AuthenticatedUser)
            ?.alias
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        if (alias != null) {
            Spacer(
                modifier = Modifier.height(4.dp),
            )

            Text(
                text = "@$alias",
                style = MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        val bio = user.bio
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        if (bio != null) {
            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    user: User,
    profileImageUrl: String?,
    imageLoader: ImageLoader,
    isUploading: Boolean,
    modifier: Modifier = Modifier,
) {
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

        if (isUploading) {
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

@Composable
private fun ProfileInformationCard(
    user: User,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = when (user) {
                    is AuthenticatedUser -> "Jouw gegevens"
                    is PublicUser -> "Profielgegevens"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(16.dp),
            )

            ProfileInfoItem(
                label = "Weergavenaam",
                value = user.displayName,
            )

            ProfileDivider()

            ProfileInfoItem(
                label = "Rol",
                value = user.role.toString(),
            )

            if (user is AuthenticatedUser) {
                AuthenticatedProfileInformation(
                    user = user,
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedProfileInformation(
    user: AuthenticatedUser,
) {
    val fullName = listOfNotNull(
        user.firstName
            ?.trim()
            ?.takeIf(String::isNotEmpty),
        user.lastName
            ?.trim()
            ?.takeIf(String::isNotEmpty),
    )
        .joinToString(separator = " ")
        .takeIf(String::isNotBlank)

    if (fullName != null) {
        ProfileDivider()

        ProfileInfoItem(
            label = "Volledige naam",
            value = fullName,
        )
    }

    val alias = user.alias
        ?.trim()
        ?.takeIf(String::isNotEmpty)

    if (alias != null) {
        ProfileDivider()

        ProfileInfoItem(
            label = "Gebruikersnaam",
            value = "@$alias",
        )
    }

    if (user.email.isNotBlank()) {
        ProfileDivider()

        ProfileInfoItem(
            label = "E-mailadres",
            value = user.email,
        )
    }

    user.birthDate?.let { birthDate ->
        ProfileDivider()

        ProfileInfoItem(
            label = "Geboortedatum",
            value = birthDate.toDisplayString(),
        )
    }
}

@Composable
private fun ProfileInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier = Modifier.height(4.dp),
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            vertical = 12.dp,
        ),
    )
}

@Composable
private fun ProfileLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileErrorContent(
    message: String,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            text = "Profiel niet beschikbaar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Button(
            onClick = onRetryClicked,
        ) {
            Text(
                text = "Opnieuw proberen",
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

private fun LocalDate.toDisplayString(): String {
    val day = dayOfMonth
        .toString()
        .padStart(
            length = 2,
            padChar = '0',
        )

    val month = monthNumber
        .toString()
        .padStart(
            length = 2,
            padChar = '0',
        )

    return "$day-$month-$year"
}