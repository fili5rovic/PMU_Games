package play.pmu.ui.theme

import androidx.compose.ui.graphics.Color
import play.pmu.domain.model.Player

/**
 * Boje se dohvataju preko [Player]-a, pa nijedna igra ne mora da pamti "igrac 1
 * je plav". Dovoljno je `player.accentColor` i svih pet mini igara automatski
 * oboji igrace isto.
 */
val Player.accentColor: Color
    get() = when (this) {
        Player.ONE -> PlayerOneColor
        Player.TWO -> PlayerTwoColor
    }

/** Podloga polovine ekrana koja pripada ovom igracu. */
val Player.panelColor: Color
    get() = when (this) {
        Player.ONE -> PlayerOneContainer
        Player.TWO -> PlayerTwoContainer
    }
