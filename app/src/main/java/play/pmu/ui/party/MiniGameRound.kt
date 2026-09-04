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

@Composable
fun MiniGameRound(
    round: PartyRound,
    gameSettings: GameSettings,
    onFinished: (RoundOutcome) -> Unit,
    modifier: Modifier = Modifier,
    roundLabel: String? = null,
) {
    LockScreenOrientation(round.game.orientation)

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
