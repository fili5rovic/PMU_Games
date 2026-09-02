package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

/**
 * Mini igre za dva igraca - one koje se igraju na podeljenom ekranu, sa
 * telefonom izmedju igraca.
 *
 * Enum nosi svoje resurse (naziv, uputstvo, ikonicu) i ono malo osobina po
 * kojima se igre razlikuju, pa se i pocetni ekran i uputstvo pre runde iscrtavaju
 * prolaskom kroz [MiniGame.entries] - nema odvojene liste koja bi mogla da se
 * raziđe sa enum-om.
 *
 * ZASTO SAMO OVE DVE OSOBINE: [needsStartingPlayer] i [orientation] su jedino
 * po cemu se tok runde stvarno razlikuje od igre do igre, pa se time izbegavaju
 * `if (game == TIC_TAC_TOE)` uslovi rasuti po kodu. Sve ostalo (velicina table,
 * dozvoljene operacije) je korisnicko PODESAVANJE i zivi u GameSettings, a ne
 * ovde - igre koje nemaju sta da podese ne dobijaju nijedno prazno polje.
 */
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
