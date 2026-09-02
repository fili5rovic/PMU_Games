package play.pmu.ui.taprace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import play.pmu.ui.components.TimerBar
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.panelColor

/**
 * Trka tapkanja. Svaka polovina ekrana je jedno veliko dugme - najveca moguca
 * meta, jer se u ovoj igri tapka brzo i bez gledanja.
 */
@Composable
fun TapRaceGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: TapRaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = winner,
                    detailRes = R.string.outcome_taps,
                    detailValue = maxOf(uiState.tapsOne, uiState.tapsTwo),
                )
            )
        }
    }

    TwoPlayerLayout(
        topContent = { TapRaceHalf(Player.TWO, uiState) },
        bottomContent = { TapRaceHalf(Player.ONE, uiState) },
        topModifier = tapHalfModifier(Player.TWO, viewModel::onTap),
        bottomModifier = tapHalfModifier(Player.ONE, viewModel::onTap),
    )
}

/**
 * Bez `indication`-a i bez ripple efekta: pri brzom tapkanju desetine ripple
 * animacija bi nepotrebno trosile vreme na iscrtavanje, a igracu ne znace nista
 * jer broj tapkanja i tako raste na ekranu.
 */
@Composable
private fun tapHalfModifier(player: Player, onTap: (Player) -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return Modifier
        .fillMaxSize()
        .background(player.panelColor)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
        ) { onTap(player) }
}

@Composable
private fun TapRaceHalf(player: Player, uiState: TapRaceUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
        modifier = Modifier.padding(PmuSpacing.large),
    ) {
        Text(
            text = uiState.tapsOf(player).toString(),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
        )
        TimerBar(
            secondsLeft = uiState.secondsLeft,
            totalSeconds = uiState.totalSeconds,
            modifier = Modifier.fillMaxWidth(TIMER_WIDTH_FRACTION),
        )
        if (!uiState.isRunning) {
            Text(
                text = stringResource(player.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}

private const val WINNER_DELAY_MILLIS = 600L
private const val TIMER_WIDTH_FRACTION = 0.7f
