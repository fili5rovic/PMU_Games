package play.pmu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import play.pmu.domain.model.Player

val isDarkTheme: Boolean
    @Composable get() = MaterialTheme.colorScheme.surface.luminance() < DARK_LUMINANCE_LIMIT

val Player.accentColor: Color
    @Composable get() = when (this) {
        Player.ONE -> if (isDarkTheme) PlayerOneColorDark else PlayerOneColor
        Player.TWO -> if (isDarkTheme) PlayerTwoColorDark else PlayerTwoColor
    }

@Composable
fun Player.areaColor(isActive: Boolean = true): Color {
    val surface = MaterialTheme.colorScheme.surface
    if (!isActive) return surface

    val alpha = if (isDarkTheme) ACTIVE_TINT_ALPHA_DARK else ACTIVE_TINT_ALPHA_LIGHT
    return accentColor.copy(alpha = alpha).compositeOver(surface)
}

private const val DARK_LUMINANCE_LIMIT = 0.5f

private const val ACTIVE_TINT_ALPHA_DARK = 0.32f
private const val ACTIVE_TINT_ALPHA_LIGHT = 0.20f
