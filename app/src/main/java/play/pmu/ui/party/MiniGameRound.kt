package play.pmu.ui.party

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import play.pmu.domain.model.GameSettings
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.party.PartyRound
import play.pmu.ui.binary.BinaryDuelGame
import play.pmu.ui.components.GameIntro
import play.pmu.ui.components.LockScreenOrientation
import play.pmu.ui.mathduel.MathDuelGame
import play.pmu.ui.memory.MemoryGame
import play.pmu.ui.reaction.ReactionGame
import play.pmu.ui.stoptimer.StopTheTimerGame
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
 * Ovde se sastaju tri stvari, i svaka dolazi sa svog mesta:
 *
 *  - [PartyRound.game] i [PartyRound.startingPlayer] iz rasporeda partije,
 *  - [gameSettings] iz korisnickih podesavanja,
 *  - a "Slucajno" u podesavanjima razresava sama igra kada runda pocne.
 *
 * Orijentacija se ne postavlja rucno nego se cita iz [MiniGame.orientation], pa
 * ovaj kod ne zna koja je igra portret a koja landscape.
 */
@Composable
fun MiniGameRound(
    round: PartyRound,
    gameSettings: GameSettings,
    onFinished: (RoundOutcome) -> Unit,
    modifier: Modifier = Modifier,
    roundLabel: String? = null,
) {
    LockScreenOrientation(round.game.orientation)

    // rememberSaveable, a ne remember: da promena konfiguracije ne bi ponovo
    // prikazala uputstvo preko igre koja je vec u toku (ViewModel igre bi
    // promenu konfiguracije prezivio, pa bi se stanja razisla).
    // Kljuc je ime igre, pa nova igra uvek pocinje od uputstva.
    var isIntroDone by rememberSaveable(round.game.name) { mutableStateOf(false) }

    if (!isIntroDone) {
        GameIntro(
            titleRes = round.game.titleRes,
            instructionRes = round.game.instructionRes,
            onStart = { isIntroDone = true },
            roundLabel = roundLabel,
            modifier = modifier,
        )
        return
    }

    // `when` nad enum-om je exhaustive: dodavanje nove mini igre nece se moci
    // prevesti dok se i ovde ne doda njen ekran.
    when (round.game) {
        MiniGame.REACTION -> ReactionGame(onFinished = onFinished)

        MiniGame.TIC_TAC_TOE -> TicTacToeGame(
            boardSizeOption = gameSettings.ticTacToeBoardSize,
            startingPlayer = round.firstPlayer,
            onFinished = onFinished,
        )

        MiniGame.MEMORY -> MemoryGame(
            startingPlayer = round.firstPlayer,
            onFinished = onFinished,
        )

        MiniGame.TAP_RACE -> TapRaceGame(onFinished = onFinished)

        MiniGame.MATH_DUEL -> MathDuelGame(
            operations = gameSettings.mathOperations,
            onFinished = onFinished,
        )

        MiniGame.STOP_THE_TIMER -> StopTheTimerGame(onFinished = onFinished)

        MiniGame.BINARY_DECIMAL -> BinaryDuelGame(onFinished = onFinished)
    }
}
