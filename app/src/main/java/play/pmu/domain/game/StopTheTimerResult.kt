package play.pmu.domain.game

import play.pmu.domain.model.Winner
import kotlin.math.abs
import kotlin.random.Random

/**
 * Ishod runde "stani na vreme": ciljno trajanje i vreme kada je svaki igrac
 * pritisnuo STOP.
 *
 * Poredjenje je izvuceno iz ViewModel-a u ovu cistu klasu, pa se pravilo
 * ("blize ciljnom vremenu pobedjuje") testira bez ijednog stvarnog cekanja -
 * test prosto zada brojeve.
 */
data class StopTheTimerResult(
    val targetMillis: Int,
    val playerOneMillis: Int,
    val playerTwoMillis: Int,
) {

    /** Odstupanje od cilja, u apsolutnoj vrednosti - rano i pozno odstupanje vrede isto. */
    val playerOneErrorMillis: Int get() = abs(playerOneMillis - targetMillis)

    val playerTwoErrorMillis: Int get() = abs(playerTwoMillis - targetMillis)

    val winner: Winner
        get() {
            val difference = playerOneErrorMillis - playerTwoErrorMillis
            return when {
                // Razlika manja od tolerancije je prakticno izjednacenje: na
                // desetinku milisekunde nema smisla deliti pobednika.
                abs(difference) <= DRAW_TOLERANCE_MILLIS -> Winner.DRAW
                difference < 0 -> Winner.PLAYER_ONE
                else -> Winner.PLAYER_TWO
            }
        }

    /** Manje odstupanje od dva, za prikaz na ekranu rezultata runde. */
    val bestErrorMillis: Int get() = minOf(playerOneErrorMillis, playerTwoErrorMillis)

    companion object {
        const val DRAW_TOLERANCE_MILLIS = 20

        /** Cilj se bira iz ovog opsega, pa se runde ne ponavljaju. */
        val TARGET_RANGE_SECONDS = 3..10

        fun randomTargetMillis(random: Random = Random.Default): Int =
            random.nextInt(TARGET_RANGE_SECONDS.first, TARGET_RANGE_SECONDS.last + 1) * 1_000
    }
}
