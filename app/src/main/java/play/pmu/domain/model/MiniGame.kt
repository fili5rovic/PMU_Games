package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

/**
 * Mini igre za dva igraca - one koje se igraju na podeljenom ekranu, u portret
 * orijentaciji, sa telefonom izmedju igraca.
 *
 * Enum nosi svoje resurse (naziv, uputstvo, ikonicu), pa i pocetni ekran i
 * uputstvo pre runde mogu da se iscrtaju prolaskom kroz [MiniGame.entries] -
 * nema odvojene liste koja bi mogla da se raziđe sa enum-om. Isti pristup vec
 * koristi i [GameType].
 *
 * Iz ove liste partija pravi svoj slucajan raspored (vidi buildPartySequence).
 */
enum class MiniGame(
    @StringRes val titleRes: Int,
    @StringRes val instructionRes: Int,
    @DrawableRes val iconRes: Int,
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
    ),
    MEMORY(
        titleRes = R.string.game_memory_title,
        instructionRes = R.string.instruction_memory,
        iconRes = R.drawable.ic_memory,
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
}
