package eu.vitamo.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.vitamo.app.features.settings.permissions.SettingsPermissionItem
import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.infrastructure.permissions.PermissionStatus
import eu.vitamo.app.ui.theme.ThemeMode
import eu.vitamo.app.ui.theme.VitaDimensions

@Composable
fun SettingsContent(
    state: SettingsState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onPermissionSelected: (AppPermission) -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    Column(
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
            )
            .padding(
                horizontal = dimensions.screenPadding,
                vertical = dimensions.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(
            dimensions.lg,
        ),
    ) {
        AppearanceSettingsCard(
            selectedThemeMode = state.themeMode,
            enabled = !state.isSaving,
            onThemeModeSelected =
                onThemeModeSelected,
        )

        if (state.missingPermissions.isNotEmpty()) {
            PermissionsSettingsCard(
                permissions = state.missingPermissions,
                permissionInProgress =
                    state.permissionInProgress,
                enabled = !state.isPermissionsLoading,
                onPermissionSelected =
                    onPermissionSelected,
            )
        } else {
            Button(onClick = onTestNotification) {
                Text(
                    text =
                        "Test Notification",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryFixedVariant,
                )
            }
        }

        if (state.isSaving) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.Center,
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(
                        dimensions.sm,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PermissionsSettingsCard(
    permissions: List<SettingsPermissionItem>,
    permissionInProgress: AppPermission?,
    enabled: Boolean,
    onPermissionSelected: (AppPermission) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

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
                .padding(
                    dimensions.cardPaddingLarge,
                ),
        ) {
            Text(
                text = "Permissies",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier =
                    Modifier.height(dimensions.xs),
            )

            Text(
                text =
                    "Voor onderstaande functies heeft VitaMo nog toegang nodig.",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(dimensions.md),
            )

            permissions.forEachIndexed {
                    index,
                    permissionItem ->

                PermissionOption(
                    item = permissionItem,
                    isLoading =
                        permissionInProgress ==
                                permissionItem.permission,
                    enabled =
                        enabled &&
                                permissionInProgress == null,
                    onClick = {
                        onPermissionSelected(
                            permissionItem.permission,
                        )
                    },
                )

                if (
                    index <
                    permissions.lastIndex
                ) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PermissionOption(
    item: SettingsPermissionItem,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = dimensions.md,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text =
                    item.permission.title(),
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight =
                    FontWeight.Medium,
            )

            Spacer(
                modifier =
                    Modifier.height(dimensions.xxs),
            )

            Text(
                text =
                    item.permission.description(),
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(
                    dimensions.lg,
                ),
            )
        } else {
            TextButton(
                onClick = onClick,
                enabled = enabled,
            ) {
                Text(
                    text =
                        item.status.actionLabel(),
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsCard(
    selectedThemeMode: ThemeMode,
    enabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

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
                .padding(dimensions.cardPaddingLarge),
        ) {
            Text(
                text = "Weergave",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(dimensions.xs),
            )

            Text(
                text = "Kies hoe VitaMo eruitziet.",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(
                modifier = Modifier.height(dimensions.md),
            )

            ThemeModeOption(
                title = "Systeeminstelling",
                description =
                    "Volg automatisch het thema van je apparaat.",
                mode = ThemeMode.SYSTEM,
                selectedMode = selectedThemeMode,
                enabled = enabled,
                onSelected = onThemeModeSelected,
            )

            HorizontalDivider()

            ThemeModeOption(
                title = "Licht",
                description =
                    "Gebruik altijd het lichte thema.",
                mode = ThemeMode.LIGHT,
                selectedMode = selectedThemeMode,
                enabled = enabled,
                onSelected = onThemeModeSelected,
            )

            HorizontalDivider()

            ThemeModeOption(
                title = "Donker",
                description =
                    "Gebruik altijd het donkere thema.",
                mode = ThemeMode.DARK,
                selectedMode = selectedThemeMode,
                enabled = enabled,
                onSelected = onThemeModeSelected,
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    title: String,
    description: String,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    enabled: Boolean,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = {
                    onSelected(mode)
                },
            )
            .padding(
                vertical = dimensions.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = mode == selectedMode,
            onClick = null,
            enabled = enabled,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = dimensions.sm,
                ),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )

            Spacer(
                modifier = Modifier.height(dimensions.xxs),
            )

            Text(
                text = description,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun AppPermission.title(): String {
    return when (this) {
        AppPermission.NOTIFICATIONS ->
            "Notificaties"
    }
}

private fun AppPermission.description(): String {
    return when (this) {
        AppPermission.NOTIFICATIONS ->
            "Ontvang meldingen over belangrijke gebeurtenissen."
    }
}

private fun PermissionStatus.actionLabel(): String {
    return when (this) {
        PermissionStatus.NOT_DETERMINED ->
            "Toestaan"

        PermissionStatus.DENIED ->
            "Opnieuw toestaan"

        PermissionStatus.DENIED_ALWAYS ->
            "Instellingen"

        PermissionStatus.GRANTED,
        PermissionStatus.PARTIAL_GRANTED ->
            "Toegestaan"

        PermissionStatus.BUSY -> "Probeer opnieuw"
    }
}