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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.game.StopTheTimerResult
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.accentColor
import play.pmu.ui.theme.areaColor
import java.util.Locale

/**
 * "Stani na vreme". Svaki igrac ima svoje STOP dugme na svojoj polovini ekrana.
 *
 * Ekran je namerno gotovo prazan: prvo samo BROJ sekundi (bez jedinice, bez
 * reci "cilj"), pa samo dugme STOP. U toku merenja se ne prikazuje nista sto bi
 * odalo proteklo vreme - ni brojac, ni traka, ni animacija - pa ovaj ekran u
 * toku merenja nema sta ni da rekomponuje.
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
        topModifier = Modifier.fillMaxSize().background(Player.TWO.areaColor()),
        bottomModifier = Modifier.fillMaxSize().background(Player.ONE.areaColor()),
    )
}

@Composable
private fun StopTimerHalf(
    player: Player,
    uiState: StopTheTimerUiState,
    onStop: (Player) -> Unit,
) {
    // AnimatedContent pretapa faze, pa je nestajanje cilja jasno vidljivo.
    AnimatedContent(
        targetState = uiState.phase,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "stopTimerPhase",
        modifier = Modifier.padding(PmuSpacing.large),
    ) { phase ->
        when (phase) {
            // Samo broj. Kontekst (igra se zove "Stani na vreme", uputstvo je
            // upravo procitano) cini jedinicu suvisnom.
            StopTheTimerPhase.SHOWING_TARGET -> Text(
                text = uiState.targetSeconds.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = player.accentColor,
                // Test procita ciljno vreme sa ekrana, pa ne mora da pogadja
                // koju je vrednost izvukao Random.
                modifier = Modifier.testTag(PmuTestTags.stopTarget(player.name)),
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

@Composable
private fun RunningHalf(
    player: Player,
    hasStopped: Boolean,
    onStop: (Player) -> Unit,
) {
    if (hasStopped) {
        Text(
            text = stringResource(R.string.stop_timer_stopped),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        return
    }

    Button(
        onClick = { onStop(player) },
        modifier = Modifier
            .fillMaxWidth(STOP_BUTTON_WIDTH)
            .height(GameTouchTargetSize)
            .testTag(PmuTestTags.stopButton(player.name)),
    ) {
        Text(
            text = stringResource(R.string.stop_timer_stop),
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

/**
 * Otkriva izmereno vreme i odstupanje. Dva broja bez jedinica: krupno je vreme
 * zaustavljanja, a ispod njega odstupanje sa znakom "+-".
 */
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
            text = formatSeconds(stopped),
            style = MaterialTheme.typography.displaySmall,
            color = player.accentColor,
        )
        Text(
            text = "±" + formatSeconds(error),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Milisekunde u citljive sekunde, npr. 4720 -> "4.72". */
private fun formatSeconds(millis: Int): String =
    String.format(Locale.getDefault(), "%.2f", millis / 1_000f)

private const val REVEAL_MILLIS = 2_600L
private const val STOP_BUTTON_WIDTH = 0.75f
