package play.pmu.domain.game

import play.pmu.domain.model.Winner
import kotlin.math.abs
import kotlin.random.Random

data class StopTheTimerResult(
    val targetMillis: Int,
    val playerOneMillis: Int,
    val playerTwoMillis: Int,
) {

    val playerOneErrorMillis: Int get() = abs(playerOneMillis - targetMillis)

    val playerTwoErrorMillis: Int get() = abs(playerTwoMillis - targetMillis)

    val winner: Winner
        get() {
            val difference = playerOneErrorMillis - playerTwoErrorMillis
            return when {
                abs(difference) <= DRAW_TOLERANCE_MILLIS -> Winner.DRAW
                difference < 0 -> Winner.PLAYER_ONE
                else -> Winner.PLAYER_TWO
            }
        }

    val bestErrorMillis: Int get() = minOf(playerOneErrorMillis, playerTwoErrorMillis)

    companion object {
        const val DRAW_TOLERANCE_MILLIS = 20

        val TARGET_RANGE_SECONDS = 3..10

        fun randomTargetMillis(random: Random = Random.Default): Int =
            random.nextInt(TARGET_RANGE_SECONDS.first, TARGET_RANGE_SECONDS.last + 1) * 1_000
    }
}
