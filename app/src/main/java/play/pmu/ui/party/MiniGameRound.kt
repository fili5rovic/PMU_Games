package play.pmu.ui.party

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.GameIntro
import play.pmu.ui.components.GameOrientation
import play.pmu.ui.components.LockScreenOrientation
import play.pmu.ui.mathduel.MathDuelGame
import play.pmu.ui.memory.MemoryGame
import play.pmu.ui.reaction.ReactionGame
import play.pmu.ui.taprace.TapRaceGame
import play.pmu.ui.tictactoe.TicTacToeGame

/**
 * Jedna runda: uputstvo, odbrojavanje, pa sama igra.
 *
 * Ovo je JEDINO mesto na kome se zna koja se igra crta za koju [MiniGame]
 * vrednost, i jedino mesto na kome se ceka uputstvo. Zato ni partija ni
 * pojedinacna igra ne ponavljaju taj tok - oba hosta pozivaju ovu funkciju i
 * samo drugacije obrade [onFinished].
 *
 * Sve mini igre se igraju na podeljenom ekranu, pa se orijentacija ovde
 * zakljucava na portret. (Pantomima, jedina landscape igra, isto tako trazi
 * svoju orijentaciju u svom ekranu.)
 */
@Composable
fun MiniGameRound(
    game: MiniGame,
    onFinished: (RoundOutcome) -> Unit,
    modifier: Modifier = Modifier,
    roundLabel: String? = null,
) {
    LockScreenOrientation(GameOrientation.PORTRAIT)

    // rememberSaveable, a ne remember: da promena konfiguracije ne bi ponovo
    // prikazala uputstvo preko igre koja je vec u toku (ViewModel igre bi
    // promenu konfiguracije prezivio, pa bi se stanja razisla).
    // Kljuc je ime igre, pa nova igra uvek pocinje od uputstva.
    var isIntroDone by rememberSaveable(game.name) { mutableStateOf(false) }

    if (!isIntroDone) {
        GameIntro(
            titleRes = game.titleRes,
            instructionRes = game.instructionRes,
            onStart = { isIntroDone = true },
            roundLabel = roundLabel,
            modifier = modifier,
        )
        return
    }

    // `when` nad enum-om je exhaustive: dodavanje nove mini igre nece se moci
    // prevesti dok se i ovde ne doda njen ekran.
    when (game) {
        MiniGame.REACTION -> ReactionGame(onFinished = onFinished)
        MiniGame.TIC_TAC_TOE -> TicTacToeGame(onFinished = onFinished)
        MiniGame.MEMORY -> MemoryGame(onFinished = onFinished)
        MiniGame.TAP_RACE -> TapRaceGame(onFinished = onFinished)
        MiniGame.MATH_DUEL -> MathDuelGame(onFinished = onFinished)
    }
}
