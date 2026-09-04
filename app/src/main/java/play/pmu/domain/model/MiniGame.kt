package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

enum class MiniGame(
    @StringRes val titleRes: Int,
    @StringRes val instructionRes: Int,
    @DrawableRes val iconRes: Int,
    /**
     * true za igre sa naizmenicnim potezima, kod kojih prvi potez daje prednost.
     * Samo takve igre dobijaju pocetnog igraca iz rasporeda partije.
     */
    val needsStartingPlayer: Boolean = false,
    val orientation: GameOrientation = GameOrientation.PORTRAIT,
) {
    REACTION(
        titleRes = R.string.game_reaction_title,
        instructionRes = R.string.instruction_reaction,
        iconRes = R.drawable.ic_reaction,
    ),
    TIC_TAC_TOE(
        titleRes = R.string.game_tictactoe_title,
        instructionRes = R.string.instruction_tictactoe,
        iconRes = R.drawable.ic_tictactoe,
        needsStartingPlayer = true,
    ),
    MEMORY(
        titleRes = R.string.game_memory_title,
        instructionRes = R.string.instruction_memory,
        iconRes = R.drawable.ic_memory,
        needsStartingPlayer = true,
    ),
    TAP_RACE(
        titleRes = R.string.game_tap_race_title,
        instructionRes = R.string.instruction_tap_race,
        iconRes = R.drawable.ic_tap_race,
    ),
    MATH_DUEL(
        titleRes = R.string.game_math_duel_title,
        instructionRes = R.string.instruction_math_duel,
        iconRes = R.drawable.ic_math,
    ),
    STOP_THE_TIMER(
        titleRes = R.string.game_stop_timer_title,
        instructionRes = R.string.instruction_stop_timer,
        iconRes = R.drawable.ic_stop_timer,
    ),
    BINARY_DECIMAL(
        titleRes = R.string.game_binary_title,
        instructionRes = R.string.instruction_binary,
        iconRes = R.drawable.ic_binary,
    ),
}
