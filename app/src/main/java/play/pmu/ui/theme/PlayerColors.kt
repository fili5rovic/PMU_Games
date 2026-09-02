package play.pmu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import play.pmu.domain.model.Player

/**
 * Da li je trenutna tema tamna.
 *
 * Cita se iz same color scheme-a, a ne iz `isSystemInDarkTheme()` - tako vazi i
 * kada je korisnik u podesavanjima rucno izabrao svetlu ili tamnu temu.
 */
val isDarkTheme: Boolean
    @Composable get() = MaterialTheme.colorScheme.surface.luminance() < DARK_LUMINANCE_LIMIT

/**
 * Boja identiteta igraca, u varijanti za trenutnu temu.
 *
 * Boje se dohvataju preko [Player]-a, pa nijedna igra ne mora da pamti "igrac 1
 * je plav" - dovoljno je `player.accentColor` i sve igre oboje igrace isto.
 */
val Player.accentColor: Color
    @Composable get() = when (this) {
        Player.ONE -> if (isDarkTheme) PlayerOneColorDark else PlayerOneColor
        Player.TWO -> if (isDarkTheme) PlayerTwoColorDark else PlayerTwoColor
    }

/**
 * Podloga dela ekrana koji pripada jednom igracu.
 *
 * Boja igraca se stavlja PREKO podloge teme sa malom prozirnoscu
 * ([compositeOver]), pa ostaje samo blag ton: u svetloj temi svetao, u tamnoj
 * taman. Zato tamna tema ostaje tamna, a tekst u boji `onSurface` je citljiv i
 * preko obojene i preko neutralne polovine.
 *
 * [isActive] = false vraca cistu podlogu teme, cime deo ekrana postaje vidno
 * neutralan. Igre sa naizmenicnim potezima time dobijaju jasnu razliku: strana
 * igraca koji je na potezu je obojena, protivnicka nije.
 */
@Composable
fun Player.areaColor(isActive: Boolean = true): Color {
    val surface = MaterialTheme.colorScheme.surface
    if (!isActive) return surface

    val alpha = if (isDarkTheme) ACTIVE_TINT_ALPHA_DARK else ACTIVE_TINT_ALPHA_LIGHT
    return accentColor.copy(alpha = alpha).compositeOver(surface)
}

private const val DARK_LUMINANCE_LIMIT = 0.5f

/** Tamna tema trazi jaci ton da bi se obojena strana uopste videla. */
private const val ACTIVE_TINT_ALPHA_DARK = 0.32f
private const val ACTIVE_TINT_ALPHA_LIGHT = 0.20f
