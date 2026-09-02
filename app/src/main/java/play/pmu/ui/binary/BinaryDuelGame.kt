package play.pmu.ui.binary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.AnswerDuelContent

/**
 * Binarno u decimalno. Ceo izgled dolazi iz [AnswerDuelContent], zajednickog sa
 * racunskim duelom - ovde ostaje samo pitanje i prijava ishoda.
 */
@Composable
fun BinaryDuelGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: BinaryDuelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.duel.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(RoundOutcome(winner = winner))
        }
    }

    AnswerDuelContent(
        prompt = stringResource(R.string.binary_prompt, uiState.question.binary),
        answers = uiState.question.answers.map { it.toString() },
        duel = uiState.duel,
        onAnswer = viewModel::onAnswer,
        promptStyle = MaterialTheme.typography.displayMedium,
        // Posle runde se prikazuje tacna decimalna vrednost.
        footer = if (winner != null) {
            stringResource(R.string.binary_correct_value, uiState.question.correctAnswer)
        } else {
            null
        },
    )
}

// Duze od ostalih igara, da igraci stignu da procitaju tacnu decimalnu vrednost.
private const val WINNER_DELAY_MILLIS = 1_500L
