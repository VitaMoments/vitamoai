package eu.vitamo.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppDimensions(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val screenPadding: Dp = 20.dp,
    val cardPadding: Dp = 12.dp,
    val cardPaddingLarge: Dp = 16.dp,
    val radiusSm: Dp = 8.dp,
    val radiusMd: Dp = 12.dp,
    val radiusLg: Dp = 16.dp,
)

internal val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }

object VitaDimensions {
    val current: AppDimensions
        @Composable get() = LocalAppDimensions.current
}

