package play.pmu.ui.stoptimer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.game.StopTheTimerResult
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.panelColor
import java.util.Locale

/**
 * "Stani na vreme". Svaki igrac ima svoje STOP dugme na svojoj polovini ekrana.
 *
 * U toku merenja se NE prikazuje nista sto bi odalo proteklo vreme - ni brojac,
 * ni traka, ni animacija. Zato ovaj ekran u toku merenja nema sta da rekomponuje.
 */
@Composable
fun StopTheTimerGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: StopTheTimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result

    LaunchedEffect(result) {
        if (result != null) {
            // Prvo se otkriju izmerena vremena i odstupanja, pa se tek onda
            // prijavljuje ishod partiji.
            delay(REVEAL_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = result.winner,
                    detailRes = R.string.outcome_error_ms,
                    detailValue = result.bestErrorMillis,
                )
            )
        }
    }

    TwoPlayerLayout(
        topContent = { StopTimerHalf(Player.TWO, uiState, viewModel::onStop) },
        bottomContent = { StopTimerHalf(Player.ONE, uiState, viewModel::onStop) },
        topModifier = Modifier.fillMaxSize().background(Player.TWO.panelColor),
        bottomModifier = Modifier.fillMaxSize().background(Player.ONE.panelColor),
    )
}

@Composable
private fun StopTimerHalf(
    player: Player,
    uiState: StopTheTimerUiState,
    onStop: (Player) -> Unit,
) {
    Column(
        modifier = Modifier.padding(PmuSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        // AnimatedContent pretapa faze, pa je nestajanje cilja jasno vidljivo.
        AnimatedContent(
            targetState = uiState.phase,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "stopTimerPhase",
        ) { phase ->
            when (phase) {
                StopTheTimerPhase.SHOWING_TARGET -> Text(
                    text = stringResource(R.string.stop_timer_target, uiState.targetSeconds),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                StopTheTimerPhase.RUNNING -> RunningHalf(
                    player = player,
                    hasStopped = uiState.stoppedMillis(player) != null,
                    onStop = onStop,
                )

                StopTheTimerPhase.FINISHED -> uiState.result?.let { result ->
                    FinishedHalf(player = player, result = result)
                }
            }
        }
    }
}

@Composable
private fun RunningHalf(
    player: Player,
    hasStopped: Boolean,
    onStop: (Player) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        if (hasStopped) {
            Text(
                text = stringResource(R.string.stop_timer_stopped),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        Button(
            onClick = { onStop(player) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = player.panelColor,
            ),
            modifier = Modifier.fillMaxWidth(STOP_BUTTON_WIDTH).heightIn(min = STOP_BUTTON_HEIGHT),
        ) {
            Text(
                text = stringResource(R.string.stop_timer_stop),
                style = MaterialTheme.typography.displaySmall,
            )
        }
        Text(
            text = stringResource(R.string.stop_timer_press_when_ready),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

/** Otkriva izmereno vreme i odstupanje - i svoje i protivnicko. */
@Composable
private fun FinishedHalf(player: Player, result: StopTheTimerResult) {
    val stopped = if (player == Player.ONE) result.playerOneMillis else result.playerTwoMillis
    val error = if (player == Player.ONE) {
        result.playerOneErrorMillis
    } else {
        result.playerTwoErrorMillis
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        Text(
            text = stringResource(R.string.stop_timer_target, result.targetMillis / 1_000),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.stop_timer_seconds, formatSeconds(stopped)),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.stop_timer_error, formatSeconds(error)),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

/** Milisekunde u citljive sekunde, npr. 4720 -> "4.72". */
private fun formatSeconds(millis: Int): String =
    String.format(Locale.getDefault(), "%.2f", millis / 1_000f)

private const val REVEAL_MILLIS = 2_600L
private const val STOP_BUTTON_WIDTH = 0.7f
private val STOP_BUTTON_HEIGHT = GameTouchTargetSize
