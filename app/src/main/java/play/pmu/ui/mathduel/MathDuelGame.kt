package play.pmu.ui.mathduel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.panelColor

/** Racunski duel: isto pitanje na obe polovine ekrana, svakom igracu uspravno. */
@Composable
fun MathDuelGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: MathDuelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = winner,
                    // Kod neresenog se dodaje objasnjenje; kada je neko pobedio,
                    // dovoljno je njegovo ime, pa detalja nema.
                    detailRes = if (winner == Winner.DRAW) R.string.outcome_nobody else null,
                )
            )
        }
    }

    TwoPlayerLayout(
        topContent = { MathHalf(Player.TWO, uiState, viewModel::onAnswer) },
        bottomContent = { MathHalf(Player.ONE, uiState, viewModel::onAnswer) },
        topModifier = Modifier.fillMaxSize().background(Player.TWO.panelColor),
        bottomModifier = Modifier.fillMaxSize().background(Player.ONE.panelColor),
    )
}

@Composable
private fun MathHalf(
    player: Player,
    uiState: MathDuelUiState,
    onAnswer: (Player, Int) -> Unit,
) {
    val canAnswer = uiState.canAnswer(player)

    Column(
        modifier = Modifier.padding(PmuSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
    ) {
        Text(
            text = uiState.question.text,
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )

        if (player in uiState.lockedOut) {
            Text(
                text = stringResource(R.string.math_duel_locked),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        // chunked(2) daje mrezu 2x2 od cetiri ponudjena odgovora.
        uiState.question.answers.chunked(ANSWERS_PER_ROW).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { answer ->
                    Button(
                        onClick = { onAnswer(player, answer) },
                        enabled = canAnswer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = player.panelColor,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = GameTouchTargetSize),
                    ) {
                        Text(
                            text = answer.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        }
    }
}

private const val ANSWERS_PER_ROW = 2
private const val WINNER_DELAY_MILLIS = 500L
