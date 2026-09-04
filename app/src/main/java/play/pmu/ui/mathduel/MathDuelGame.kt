package play.pmu.ui.mathduel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.AnswerDuelContent

@Composable
fun MathDuelGame(
    operations: Set<MathOperation>,
    onFinished: (RoundOutcome) -> Unit,
    viewModel: MathDuelViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.startRound(operations) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val question = uiState.question ?: return
    val winner = uiState.duel.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(RoundOutcome(winner = winner))
        }
    }

    AnswerDuelContent(
        prompt = question.text,
        answers = question.answers.map { it.toString() },
        duel = uiState.duel,
        onAnswer = viewModel::onAnswer,
    )
}

private const val WINNER_DELAY_MILLIS = 500L
